#!/usr/bin/env python3

import unittest

"""
A set of common functions used in the source information extraction scripts
"""

import sys, os, re

ARDUINO_FILES = {'.ino', '.h', '.c', '.hpp', '.cpp'}

def removeStarGutter(msg, replacement=' '):
    """ Clean the text inside a c++ multiline comment's left margin *'s """
    return re.sub(r'\n\s*\* ?', replacement, msg, flags=re.MULTILINE)

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

class TestRemoveStarGutter(unittest.TestCase):
    def test_one_line(self):
        self.assertEqual(removeStarGutter('Simple Sample'), 'Simple Sample')

    def test_remove_newlines(self):
        input = """A
                 * B
                 * C
                 *"""
        expected = """A B C """
        self.assertEqual(removeStarGutter(input), expected)

    def test_optional_space(self):
        input = """A
                 *B
                 * C
                 *D
                 *"""
        expected = """A B C D """
        self.assertEqual(removeStarGutter(input), expected)

    def test_replace_break(self):
        input = """A
                 * B
                 * C
                 *"""
        expected = """A<br>B<br>C<br>"""
        self.assertEqual(removeStarGutter(input, '<br>'), expected)

class TestHasExtension(unittest.TestCase):
    def test_input_matches(self):
        extList = [".a",".b"]
        self.assertTrue(hasExtension("yes.a", extList))
        self.assertTrue(hasExtension("yes.b", extList))

    def test_input_empty(self):
        extList = [".a",".b"]
        self.assertFalse(hasExtension("",extList))

    def test_input_doesnt_match(self):
        extList = [".a",".b"]
        self.assertFalse(hasExtension("nope",extList))
        self.assertFalse(hasExtension("nope.c",extList))
        self.assertFalse(hasExtension("nope.a.c",extList))

    def test_extension_list_empty(self):
        extList = []
        self.assertFalse(hasExtension("maybe.a", extList))

if __name__ == '__main__':
    unittest.main()
