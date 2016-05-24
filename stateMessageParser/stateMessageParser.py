#!/usr/bin/env python3

description = """State Message Parser

Parses all the code in the MINDS-i-Drone project looking for comments formatted
to contain /*# followed by a state name (no spaces). All text following the
first space after the state name is considered the description.
/*#STATE_NAME ...description...*/

The names and description will be dumped to an XML file the Dashboard can use
to provide more detailed information about errors it receives containing the
state name string.

The script requires two arguments;
the folder path to search within,
followed by the destination filename for XML message database"""

import sys, os, re
import xml.etree.ElementTree as ET

FILETYPES = {'.ino', '.h', '.c', '.hpp', '.cpp'}
MSG_REGEX = re.compile("""/\*#(\w*) (.*)\*/""")

def hasExtension(name, extList):
    """ Returns true if `name` has an extension that appears in `extList` """
    _, foundExt = os.path.splitext(name)
    return any(map(lambda e: foundExt == e, extList))

def filesUnder(directory):
    """ Returns a list of files found recursivly under `directory` """
    files = list()
    for root,_,names in os.walk(directory):
        files += [os.path.join(root,n) for n in names]
    return files

def getMatches(file, regex):
    """ Returns a list of all passages in `file` matching `regex` """
    source = open(file, "r")
    return [x for x in regex.findall(source.read())]

class Message:
    def __init__(self, path, match):
        self.name = match[0]
        self.path = path
        self.text = match[1]

# check args
if len(sys.argv) != 3:
    print(description)
    exit(1)
searchDir = sys.argv[1]
destFile = sys.argv[2]

# find messages
filesToScan = [f for f in filesUnder(searchDir) if hasExtension(f,FILETYPES)]
messages = list()
for f in filesToScan:
    path = f[len(searchDir):] # show path relative to search folder
    messages += [Message(path,m) for m in getMatches(f, MSG_REGEX)]

# check for name collisions
usedNames = set()
for m in messages:
    if m.name in usedNames:
        print("Name Collision Detected on "+m.name)
        for col in messages:
             if col.name==m.name: print("Used in "+m.path)
        exit(1)
    usedNames.add(m.name)

# dump to XML
xmlRoot = ET.Element('MessageDB')
for m in messages:
    e = ET.SubElement(xmlRoot, "Message")
    e.set("path",m.path)
    e.set("name",m.name)
    e.text = m.text
ET.ElementTree(xmlRoot).write(destFile)
