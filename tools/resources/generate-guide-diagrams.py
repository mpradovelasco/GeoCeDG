#!/usr/bin/env python3
# Copyright (c) 2026 GeoCeDG contributors
# SPDX-License-Identifier: EUPL-1.2

"""Render the original CU9-R1 diagrams shipped with the bilingual user guide.

Run from any directory with Python 3.10+, Matplotlib 3.11.2 and Pillow 12.3.0.
Use --check to compare a fresh render to the tracked PNG bytes. The pictures
are CC-BY-4.0. This build-time tool imports no product or upstream artwork.
"""

from __future__ import annotations

import argparse
from io import BytesIO
import math
from pathlib import Path

import matplotlib as mpl
from matplotlib.axes import Axes
from matplotlib.backends.backend_agg import FigureCanvasAgg
from matplotlib.figure import Figure
from matplotlib.patches import Circle, FancyBboxPatch, Polygon


WIDTH = 1440
DPI = 120
INK = "#17324a"
MUTED = "#465e72"
AXIS = "#8294a2"
GRID = "#e4ebef"
PAPER = "#ffffff"
FIELD = "#f4f8fa"
BLUE = "#2456a6"
BLUE_TINT = "#e6efff"
TEAL = "#0f6f66"
TEAL_TINT = "#dff3ed"
RED = "#a64545"
RED_TINT = "#faeaea"

Point = tuple[float, float]


def label(ax: Axes, x: float, y: float, value: str, *, size: int = 32,
          color: str = INK, align: str = "center", weight: str = "normal") -> None:
    ax.text(x, y, value, fontsize=size * 72 / DPI, color=color,
            ha=align, va="center", fontweight=weight)


def path(ax: Axes, points: list[Point], color: str = INK,
         width: float = 4, *, dash: bool = False, layer: int = 2) -> None:
    xs, ys = zip(*points)
    ax.plot(xs, ys, color=color, linewidth=width * 72 / DPI,
            solid_capstyle="round", solid_joinstyle="round",
            linestyle=(0, (3, 4)) if dash else "-", zorder=layer)


def panel(ax: Axes, x: float, y: float, width: float, height: float,
          *, fill: str = FIELD, edge: str = GRID, radius: int = 18,
          layer: int = 1) -> None:
    ax.add_patch(FancyBboxPatch((x, y), width, height,
                                boxstyle=f"round,pad=0,rounding_size={radius}",
                                facecolor=fill, edgecolor=edge, linewidth=1.2, zorder=layer))


def dot(ax: Axes, at: Point, *, radius: float = 8, fill: str = INK,
        edge: str = PAPER, width: float = 3) -> None:
    ax.add_patch(Circle(at, radius, facecolor=fill, edgecolor=edge,
                        linewidth=width * 72 / DPI, zorder=5))


def arrowhead(ax: Axes, tip: Point, direction: Point,
              color: str = INK, size: float = 20) -> None:
    dx, dy = direction
    length = math.hypot(dx, dy)
    ux, uy = dx / length, dy / length
    x, y = tip
    back_x, back_y = x - ux * size, y - uy * size
    half_width = size * 0.40
    vertices = [tip, (back_x - uy * half_width, back_y + ux * half_width),
                (back_x + uy * half_width, back_y - ux * half_width)]
    ax.add_patch(Polygon(vertices, closed=True, facecolor=color,
                         edgecolor=color, linewidth=0, zorder=4))


def arrow(ax: Axes, start: Point, end: Point, *, color: str = MUTED,
          width: float = 4, size: float = 20) -> None:
    path(ax, [start, end], color, width)
    arrowhead(ax, end, (end[0] - start[0], end[1] - start[1]), color, size)


def circle_point(cx: float, cy: float, radius: float, angle: float) -> Point:
    return cx + radius * math.cos(angle), cy - radius * math.sin(angle)


def arc(ax: Axes, cx: float, cy: float, radius: float, start: float,
        end: float, color: str, width: float) -> None:
    # The sampled path only renders the analytic arc. The labels are lengths
    # on the unit circle, independent of the displayed radius or point count.
    count = max(2, math.ceil(abs(end - start) / 0.016))
    points = [circle_point(cx, cy, radius, start + (end - start) * i / count)
              for i in range(count + 1)]
    path(ax, points, color, width)


def locuslength_cycle(ax: Axes) -> None:
    start, end = 2.5, -2.5
    routes = [
        ("F", end + math.tau, 2.84, TEAL),
        ("R", end, -0.26, BLUE),
        ("C", start + math.tau, 5.10, TEAL),
    ]
    for index, (name, finish, angle, color) in enumerate(routes):
        left = 28 + 470 * index
        panel(ax, left, 22, 444, 398)
        label(ax, left + 26, 58, name, size=36, color=color,
              align="left", weight="bold")
        length = f"{abs(finish - start):.6f}".rstrip("0").rstrip(".")
        label(ax, left + 415, 58, f"≈ {length}", size=34, color=color,
              align="right", weight="bold")
        cx, cy, radius = left + 234, 246, 117
        path(ax, [(cx - radius - 19, cy), (cx + radius + 17, cy)], GRID, 2, dash=True)
        path(ax, [(cx, cy - radius - 16), (cx, cy + radius + 15)], GRID, 2, dash=True)
        arc(ax, cx, cy, radius, 0, math.tau, AXIS, 3)
        arc(ax, cx, cy, radius, start, finish, color, 8)
        direction = 1 if finish > start else -1
        arrowhead(ax, circle_point(cx, cy, radius, angle),
                  (-direction * math.sin(angle), -direction * math.cos(angle)),
                  color, size=26)
        seam = circle_point(cx, cy, radius, math.pi)
        dot(ax, seam, radius=7, fill=PAPER, edge=MUTED, width=3)
        label(ax, seam[0] - 16, seam[1], "±π", size=25, align="right", color=MUTED)
        ends = [("A", start, -13)] if name == "C" else [("A", start, -13), ("B", end, 13)]
        for endpoint, parameter, offset in ends:
            at = circle_point(cx, cy, radius, parameter)
            dot(ax, at)
            label(ax, at[0] - 17, at[1] + offset, endpoint, align="right", weight="bold")


def graph(ax: Axes, left: float, *, origin: Point,
          top: float = 96, bottom: float = 372, grid_spacing: int = 57) -> None:
    ox, oy = origin
    for offset in (-3, -2, -1, 1, 2, 3):
        x = ox + offset * grid_spacing
        if left + 35 < x < left + 575:
            path(ax, [(x, top), (x, bottom)], GRID, 2)
        y = oy + offset * grid_spacing
        if top < y < bottom:
            path(ax, [(left + 35, y), (left + 575, y)], GRID, 2)
    path(ax, [(left + 35, oy), (left + 575, oy)], AXIS, 2)
    path(ax, [(ox, top), (ox, bottom)], AXIS, 2)


def semantic_point_address(ax: Axes) -> None:
    for left, h in [(28, 0), (802, 2)]:
        panel(ax, left, 22, 610, 388)
        label(ax, left + 25, 58, f"h = {h}", align="left", weight="bold")
        ox, oy, scale = left + 305, 302, 69
        graph(ax, left, origin=(ox, oy), grid_spacing=scale)
        cy = oy - scale * h
        path(ax, [(ox - 2 * scale, cy), (ox + 2 * scale, cy)], BLUE, 8)
        for name, unit in [("A", -2), ("P", -1), ("Q", 1), ("D", 2)]:
            x = ox + unit * scale
            end = abs(unit) == 2
            dot(ax, (x, cy), radius=9 if end else 12,
                fill=PAPER if end else TEAL, edge=BLUE if end else PAPER, width=3)
            label(ax, x, cy - 26, name, size=28, weight="bold")
    arrow(ax, (670, 215), (770, 215), color=TEAL, size=28)


def intersection_inspector_flow(ax: Axes) -> None:
    for left, materialized in [(28, False), (802, True)]:
        panel(ax, left, 22, 610, 388)
        label(ax, left + 25, 58, "X" if materialized else "R",
              align="left", weight="bold", color=TEAL)
        ox, oy, scale = left + 305, 245, 95
        graph(ax, left, origin=(ox, oy), grid_spacing=scale)
        arc(ax, ox, oy, scale, 0, math.tau, TEAL, 5)
        path(ax, [(ox - 2 * scale, oy), (ox + 2 * scale, oy)], BLUE, 6)
        label(ax, ox - 2 * scale - 15, oy - 26, "S", size=28, color=BLUE, weight="bold")
        label(ax, ox + scale - 7, oy - 82, "c", size=28, color=TEAL, weight="bold")
        if materialized:
            dot(ax, (ox - scale, oy), radius=14, fill=TEAL)
            label(ax, ox - scale - 25, oy + 32, "X", size=31, color=TEAL, weight="bold")
        else:
            for unit, coordinate in [(-1, "x = −1"), (1, "x = 1")]:
                dot(ax, (ox + scale * unit, oy), radius=11, fill=PAPER, edge=TEAL, width=4)
                label(ax, ox + (scale + 56) * unit, oy + 34, coordinate,
                      size=29, color=MUTED, weight="bold")
    arrow(ax, (670, 215), (770, 215), color=TEAL, size=28)


def drawing_page(ax: Axes, x: float, y: float, *, source: bool, receiver: bool,
                 width: float = 108, height: float = 136) -> None:
    fold = 24
    corners = [(x, y), (x + width - fold, y), (x + width, y + fold),
               (x + width, y + height), (x, y + height)]
    ax.add_patch(Polygon(corners, facecolor=PAPER, edgecolor=AXIS, linewidth=2))
    path(ax, [(x + width - fold, y), (x + width - fold, y + fold),
              (x + width, y + fold)], AXIS, 2)
    if source:
        middle = x + width * 0.52
        path(ax, [(middle - 23, y + 69), (middle, y + 39),
                  (middle + 23, y + 69), (middle - 23, y + 69)], TEAL, 4)
    if receiver:
        path(ax, [(x + 25, y + 111), (x + width - 24, y + 86)], BLUE, 4)
        dot(ax, (x + 25, y + 111), radius=5, fill=BLUE, width=1.5)
        dot(ax, (x + width - 24, y + 86), radius=5, fill=BLUE, width=1.5)


def insertion_review_flow(ax: Axes) -> None:
    for index in range(3):
        left = 28 + index * 470
        panel(ax, left, 22, 444, 370)
        dot(ax, (left + 38, 58), radius=20, fill=TEAL, edge=TEAL, width=0)
        ax.text(left + 38, 58, str(index + 1), fontsize=19, color=PAPER,
                ha="center", va="center", fontweight="bold", zorder=6)
    drawing_page(ax, 76, 134, source=True, receiver=False)
    label(ax, 249, 203, "+", color=MUTED, size=40)
    drawing_page(ax, 314, 134, source=False, receiver=True)
    # The offset outline denotes a temporary copy rather than a second window.
    panel(ax, 652, 145, 116, 146, fill=TEAL_TINT, edge=TEAL, radius=6)
    drawing_page(ax, 670, 124, source=True, receiver=True)
    for left in [456, 926]:
        arrow(ax, (left, 207), (left + 58, 207), color=TEAL, size=22)
    path(ax, [(1029, 207), (1084, 207)], MUTED)
    path(ax, [(1084, 130), (1084, 286)], MUTED)
    arrow(ax, (1084, 130), (1219, 130), color=TEAL, size=18)
    arrow(ax, (1084, 286), (1219, 286), color=RED, size=18)
    dot(ax, (1151, 130), radius=18, fill=TEAL_TINT, edge=TEAL, width=2)
    dot(ax, (1151, 286), radius=18, fill=RED_TINT, edge=RED, width=2)
    ax.text(1151, 130, "✓", fontsize=17, color=TEAL, ha="center", va="center", zorder=6)
    ax.text(1151, 286, "×", fontsize=21, color=RED, ha="center", va="center", zorder=6)
    drawing_page(ax, 1250, 62, source=True, receiver=True)
    drawing_page(ax, 1250, 218, source=False, receiver=True)


def controls_panel(ax: Axes, x: float, y: float, name: str, color: str, fill: str) -> None:
    panel(ax, x, y, 122, 94, fill=fill, edge=color, radius=10, layer=3)
    label(ax, x + 20, y + 25, name, color=color, size=29, align="left", weight="bold")
    path(ax, [(x + 19, y + 61), (x + 102, y + 61)], color, 3, layer=4)
    dot(ax, (x + 64, y + 61), radius=7, fill=color, width=1)


def control_panel_anchors(ax: Axes) -> None:
    for left, dx, dy in [(28, 0, 0), (802, 100, -42)]:
        panel(ax, left, 22, 610, 388)
        graph(ax, left, origin=(left + 241 + dx, 237 + dy), top=61, bottom=373,
              grid_spacing=50)
        controls_panel(ax, left + 72, 78, "F", BLUE, BLUE_TINT)
        controls_panel(ax, left + 318 + dx, 219 + dy, "W", TEAL, TEAL_TINT)
    arrow(ax, (670, 215), (770, 215), color=TEAL, size=28)


DIAGRAMS = {
    "locuslength-cycle": (444, locuslength_cycle),
    "semantic-point-address": (432, semantic_point_address),
    "intersection-inspector-flow": (432, intersection_inspector_flow),
    "insertion-review-flow": (414, insertion_review_flow),
    "control-panel-anchors": (432, control_panel_anchors),
}


def render(name: str) -> bytes:
    height, draw = DIAGRAMS[name]
    figure = Figure(figsize=(WIDTH / DPI, height / DPI), dpi=DPI, facecolor=PAPER)
    ax = figure.add_axes((0, 0, 1, 1))
    ax.set_xlim(0, WIDTH)
    ax.set_ylim(height, 0)
    ax.set_axis_off()
    draw(ax)
    buffer = BytesIO()
    FigureCanvasAgg(figure).print_png(buffer, metadata={
        "Software": "GeoCeDG guide diagram generator",
        "Title": name,
        "Author": "GeoCeDG contributors",
        "Copyright": "2026 GeoCeDG contributors, CC-BY-4.0",
        "Description": "Original conceptual diagram for the GeoCeDG user guide.",
    })
    return buffer.getvalue()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="compare rendered PNG bytes")
    args = parser.parse_args()
    directory = Path(__file__).resolve().parents[2] / "docs" / "user" / "images"
    if not args.check:
        directory.mkdir(parents=True, exist_ok=True)
    invalid = []
    with mpl.rc_context({"font.family": "DejaVu Sans", "text.usetex": False}):
        for name, (height, _) in DIAGRAMS.items():
            destination = directory / f"{name}.png"
            content = render(name)
            if args.check:
                if not destination.exists() or destination.read_bytes() != content:
                    invalid.append(name)
            else:
                destination.write_bytes(content)
            print(f"{name}: {WIDTH} x {height}")
    if invalid:
        print("Missing or different: " + ", ".join(invalid))
    return int(bool(invalid))


if __name__ == "__main__":
    raise SystemExit(main())
