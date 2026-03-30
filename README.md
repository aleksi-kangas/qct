# QCT Tools

[![Build](https://github.com/aleksi-kangas/qct/actions/workflows/workflow.yaml/badge.svg)](https://github.com/aleksi-kangas/qct/actions/workflows/workflow.yaml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=aleksi-kangas_qct&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=aleksi-kangas_qct)

> Tools for working with Quick Chart File (`.QCT`) maps in Java

## Motivation

This project was inspired by the desire to convert a few of [Ordnance Survey](https://www.ordnancesurvey.co.uk/) UK maps
that I have from the proprietary `.qct` format into more widely accessible formats for use in image viewers and GIS
software. It provides an efficient Java implementation of
the [Quick Chart (.QCT) File Format Specification (v1.03)](https://www.etheus.net/Quick_Chart_File_Format).

![](map.png)
*A small sample of a `.png` file exported from a `.qct` file, depicting the famous Ben Nevis, the highest point in the
UK. All map rights reserved by Ordnance Survey.*

## Features

- **Decoding**
    - [x] [Huffman-Coded](http://en.wikipedia.org/wiki/Huffman_coding) tiles
    - [x] [Run-Length-Encoded (RLE)](http://en.wikipedia.org/wiki/Run-length_encoding) tiles
    - [ ] *Pixel-Packed* tiles
        - *Note:* Not implemented, as I have yet to come across any `.qct` file with _Pixel-Packed_ tiles.
          Feel free to open an issue with a sample `.qct` file for implementation and testing purposes.
- **Encoding**
    - [x] [Run-Length-Encoded (RLE)](http://en.wikipedia.org/wiki/Run-length_encoding) tiles
    - [ ] [Huffman-Coded](http://en.wikipedia.org/wiki/Huffman_coding) tiles
    - [ ] *Pixel-Packed* tiles
        - *Note:* Not implemented, as I have yet to come across any `.qct` file with _Pixel-Packed_ tiles.
          Feel free to open an issue with a sample `.qct` file for implementation and testing purposes.

##### Acknowledgements

This project would not be possible without the incredible work of Craig Shelley and Mark Bryant on
the [Quick Chart (.QCT) File Format Specification v1.03](https://www.etheus.net/Quick_Chart_File_Format).
