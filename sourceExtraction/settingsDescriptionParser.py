#!/usr/bin/env python3

description = """Setting Description Parser

Parses all the code in the MINDS-i-Drone project looking for comments formatted
to contain /*SETTINGTAG followed xml-style attributes, then any extra lines have
their left gutter stripped off and become the html description of each setting.

e.g.
/*SETTING index="0" name="Gain" min="0.0" max="1.0" def="0.003"
 * Full length setting
 * description over multiple lines
 */

The names and description will be dumped to an XML file the Dashboard can use
to provide more detailed information in its setting configuration screen

The script requires two arguments;
the folder path to search within,
followed by the destination directory for XML setting databases"""

import sys, os, re
import xml.etree.ElementTree as ET
from common import *

SETTING_TYPES = [("AIRSETTING","airSettings.xml"),
                 ("GROUNDSETTING","groundSettings.xml")]
MSG_REGEX = re.compile("""/\*(\w+)\s([^\n]*)(.*?)\*/""", re.DOTALL)

# check args
if len(sys.argv) != 3:
    print(description)
    exit(1)
searchDir = sys.argv[1]
destFilePath = sys.argv[2]

filesToScan = [f for f in filesUnder(searchDir) if hasExtension(f,ARDUINO_FILES)]
potentialMatches = []
for f in filesToScan:
    potentialMatches += getMatches(f,MSG_REGEX)

def findMessageByName(name):
    messages = []
    for m in potentialMatches:
        if(m[0] == name):
            messages.append((m[1], removeStarGutter(m[2])))
    return messages

for name,file in SETTING_TYPES:
    messages = findMessageByName(name)
    # dump to XML
    xmlRoot = ET.Element('settingList')
    for m in messages:
        e = ET.SubElement(xmlRoot, "setting")
        attributeRE = re.compile("(\S*)=\"(.*?)\"")
        for a in attributeRE.findall(m[0]):
            e.set(a[0], a[1])
        e.text = m[1]
    ET.ElementTree(xmlRoot).write(os.path.join(destFilePath,file))

