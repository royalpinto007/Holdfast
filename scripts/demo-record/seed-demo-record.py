"""Rebuild the Holdfast demo record: three photo-like images and a valid chain.

The images are mock content for the store listing, drawn rather than
photographed. They are drawn to read as photographs at thumbnail size, because
a grey rectangle with a caption in it reads as an unfinished app.

The chain is recomputed here exactly the way Chain.kt computes it, so the app
verifies the seeded record with the same code a real one goes through.
"""

import hashlib
import json
import math
import os
import random

from PIL import Image, ImageDraw, ImageFilter

W, H = 1200, 900
OUT = os.path.dirname(os.path.abspath(__file__))
random.seed(7)


def grain(img, amount=22):
    """Sensor noise and surface texture, at several scales.

    A single smooth gradient plus fine noise still reads as vector art. Real
    plaster and laminate vary in broad patches as well as per-pixel, so the
    coarse octave is upscaled from a small noise field and the fine one is
    laid over it.
    """
    fine = Image.effect_noise((W, H), amount).convert("L")
    coarse = Image.effect_noise((W // 14, H // 14), amount * 2).convert("L")
    coarse = coarse.resize((W, H), Image.BICUBIC).filter(ImageFilter.GaussianBlur(6))
    img = Image.blend(img, Image.merge("RGB", (coarse,) * 3), 0.10)
    return Image.blend(img, Image.merge("RGB", (fine,) * 3), 0.085)


def vignette(img, strength=0.30):
    mask = Image.new("L", (W, H), 0)
    d = ImageDraw.Draw(mask)
    d.ellipse((-W * 0.25, -H * 0.35, W * 1.25, H * 1.35), fill=255)
    mask = mask.filter(ImageFilter.GaussianBlur(160))
    dark = Image.new("RGB", (W, H), (0, 0, 0))
    return Image.composite(img, Image.blend(img, dark, strength), mask)


def blob(draw, cx, cy, rx, ry, colour, points=180, wobble=0.22, seed=None):
    """An irregular closed shape, wobbling smoothly rather than per vertex.

    Drawing each vertex at an independently random radius gives a sawtooth star,
    which is what a first attempt here produced. A stain or a burn varies at low
    frequency: it bulges in two or three directions over the whole outline. So
    the radius is a sum of a few harmonics with random phase, which stays smooth
    all the way round and still closes on itself.
    """
    rng = random.Random(seed) if seed is not None else random
    harmonics = [(k, rng.uniform(0.35, 1.0) / k, rng.uniform(0, 2 * math.pi)) for k in (2, 3, 5)]
    pts = []
    for i in range(points):
        a = 2 * math.pi * i / points
        r = 1 + wobble * sum(amp * math.sin(k * a + phase) for k, amp, phase in harmonics)
        pts.append((cx + math.cos(a) * rx * r, cy + math.sin(a) * ry * r))
    draw.polygon(pts, fill=colour)


def hallway():
    """Painted wall above a skirting board, with a scuff where a bag has hit it."""
    img = Image.new("RGB", (W, H), (232, 228, 220))
    d = ImageDraw.Draw(img)
    # Light falls off away from the window, so the wall is not one flat tone.
    for y in range(H):
        t = y / H
        d.line([(0, y), (W, y)], fill=(int(236 - 26 * t), int(232 - 26 * t), int(224 - 24 * t)))
    # Nobody holds a phone square to a wall, so the skirting runs off level.
    skirt_l, skirt_r = int(H * 0.735), int(H * 0.775)
    floor_l, floor_r = int(H * 0.855), int(H * 0.895)
    d.polygon([(0, skirt_l), (W, skirt_r), (W, floor_r), (0, floor_l)], fill=(243, 241, 236))
    d.line([(0, skirt_l), (W, skirt_r)], fill=(206, 202, 194), width=5)
    d.polygon([(0, floor_l), (W, floor_r), (W, H), (0, H)], fill=(146, 112, 78))
    for i in range(9):  # floorboard seams, following the same skew
        d.line([(0, floor_l + i * 15), (W, floor_r + i * 15)], fill=(132, 100, 70), width=1)

    # Light pools away from the skirting; the corner under it stays darker.
    shade = Image.new("L", (W, H), 0)
    ImageDraw.Draw(shade).polygon(
        [(0, skirt_l - 90), (W, skirt_r - 90), (W, skirt_r), (0, skirt_l)], fill=90
    )
    shade = shade.filter(ImageFilter.GaussianBlur(45))
    img = Image.composite(Image.new("RGB", (W, H), (120, 116, 110)), img, shade)
    d = ImageDraw.Draw(img)

    scuff = Image.new("RGB", (W, H), (198, 194, 188))
    sd = ImageDraw.Draw(scuff)
    mask = Image.new("L", (W, H), 0)
    md = ImageDraw.Draw(mask)
    for i in range(7):
        cx = 470 + i * 34 + random.randint(-12, 12)
        cy = skirt_l - 70 + random.randint(-16, 16)
        blob(md, cx, cy, 46, 20, random.randint(90, 170), wobble=0.16, seed=i)
        blob(sd, cx, cy, 46, 20, (74, 68, 62), wobble=0.16, seed=i)
    mask = mask.filter(ImageFilter.GaussianBlur(9))
    img = Image.composite(scuff, img, mask)
    return vignette(grain(img))


def bathroom():
    """Ceiling with a damp patch: a stained ring, darker at the edge than the middle."""
    img = Image.new("RGB", (W, H), (238, 240, 241))
    d = ImageDraw.Draw(img)
    for y in range(H):
        t = y / H
        d.line([(0, y), (W, y)], fill=(int(243 - 20 * t), int(244 - 18 * t), int(245 - 16 * t)))
    d.line([(0, int(H * 0.12)), (W, int(H * 0.08))], fill=(220, 221, 222), width=3)

    stain = Image.new("RGB", (W, H), (223, 214, 197))
    sd = ImageDraw.Draw(stain)
    mask = Image.new("L", (W, H), 0)
    md = ImageDraw.Draw(mask)
    cx, cy = 620, 460
    blob(md, cx, cy, 245, 180, 210, wobble=0.16, seed=3)   # dried edge, darkest
    blob(sd, cx, cy, 245, 180, (168, 136, 92), wobble=0.16, seed=3)
    blob(md, cx, cy, 188, 136, 120, wobble=0.16, seed=3)   # centre, lighter
    blob(sd, cx, cy, 188, 136, (207, 187, 152), wobble=0.16, seed=3)
    mask = mask.filter(ImageFilter.GaussianBlur(30))
    img = Image.composite(stain, img, mask)
    return vignette(grain(img), 0.22)


def kitchen():
    """Worktop beside the hob, with a burn mark where something hot was set down."""
    img = Image.new("RGB", (W, H), (206, 182, 148))
    d = ImageDraw.Draw(img)
    for y in range(H):
        t = y / H
        d.line([(0, y), (W, y)], fill=(int(214 - 30 * t), int(190 - 28 * t), int(155 - 26 * t)))
    for _ in range(240):  # laminate grain
        y = random.randint(0, H)
        d.line(
            [(0, y), (W, y + random.randint(-6, 6))],
            fill=(random.randint(180, 200), random.randint(158, 176), random.randint(126, 142)),
            width=random.choice([1, 1, 2]),
        )
    img = img.filter(ImageFilter.GaussianBlur(0.7))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 250, H], fill=(38, 38, 40))  # hob, out of focus at the edge
    d.rectangle([250, 0, 262, H], fill=(96, 96, 100))

    burn = Image.new("RGB", (W, H), (176, 148, 112))
    bd = ImageDraw.Draw(burn)
    mask = Image.new("L", (W, H), 0)
    md = ImageDraw.Draw(mask)
    cx, cy = 640, 470
    blob(md, cx, cy, 180, 150, 150, wobble=0.15, seed=11)
    blob(bd, cx, cy, 180, 150, (118, 82, 48), wobble=0.15, seed=11)
    blob(md, cx, cy, 118, 96, 240, wobble=0.15, seed=11)
    blob(bd, cx, cy, 118, 96, (52, 33, 22), wobble=0.15, seed=11)
    mask = mask.filter(ImageFilter.GaussianBlur(12))
    img = Image.composite(burn, img, mask)
    return vignette(grain(img), 0.26)


FIELD_SEP = "\x1f"  # written as an escape; a separator you cannot see is one you cannot check
GENESIS = "0" * 64


def sha(b):
    return hashlib.sha256(b).hexdigest()


def seal(entry, prev):
    pre = FIELD_SEP.join([
        entry["id"],
        str(entry["at"]),
        entry["note"],
        entry["photoHash"] or "-",
        entry["place"] or "-",
        prev,
    ])
    return sha(pre.encode())


images = {"p1.jpg": hallway(), "p2.jpg": bathroom(), "p3.jpg": kitchen()}
hashes = {}
for name, im in images.items():
    path = os.path.join(OUT, name)
    im.save(path, "JPEG", quality=86)
    hashes[name] = sha(open(path, "rb").read())

case = json.load(open(os.path.join(OUT, "case.json")))
prev = GENESIS
for e in case["entries"]:
    e["photoHash"] = hashes[e["photoFile"]]
    e["prev"] = prev
    e["hash"] = seal(e, prev)
    prev = e["hash"]
json.dump(case, open(os.path.join(OUT, "case.json"), "w"), indent=2)
print("head", prev)
