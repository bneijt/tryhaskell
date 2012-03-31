#!/bin/bash
cd "`dirname $0`"
bitmap="logo.png"
#gst-launch filesrc location=logo.svg ! gdkpixbufdec ! pngenc ! filesink location="$bitmap"
inkscape --without-gui --file=logo.svg --export-png="$bitmap" --export-width=512 --export-height=512
convert "$bitmap" -scale 72x72 ../tryhaskell/res/drawable-hdpi/ic_launcher.png
convert "$bitmap" -scale 36x36 ../tryhaskell/res/drawable-ldpi/ic_launcher.png
convert "$bitmap" -scale 48x48 ../tryhaskell/res/drawable-mdpi/ic_launcher.png
