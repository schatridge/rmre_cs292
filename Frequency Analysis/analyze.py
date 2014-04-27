from __future__ import print_function
from math import sqrt
import os
import json

def getTitle(textId):
    title = ''
    with open('./good_books/' + textId + '.txt') as g:
        for line in g:
            if ('Title:' in line):
                title = line.replace('Title:', '').strip()
                break
    return title

gWordCounts = {}
wordDiff = {}
highestFreq = 0

with open('gutenout/GLOBAL_counts.txt') as f:
    for line in f:
        arr = line.split('\t')

        count = int(arr[1])
        gWordCounts[arr[0]] = count
        if (highestFreq < count):
            highestFreq = count

for word in gWordCounts:
    normalized = gWordCounts[word] * 100.0 / highestFreq
    # Manually constructing percentiles for difficulty
    if (normalized <= 100.0 and normalized > 53):
        normalized = 0
    elif (normalized <= 50 and normalized > 18):
        normalized = 10
    elif (normalized <= 18 and normalized > 10):
        normalized = 20.0
    elif (normalized <= 10 and normalized > 5):
        normalized = 30.0
    elif (normalized <= 5 and normalized > 2.2):
        normalized = 40.0
    elif (normalized <= 2.2 and normalized > 1):
        normalized = 50.0
    elif (normalized <= 1 and normalized > 0.48):
        normalized = 60.0
    elif (normalized <= 0.48 and normalized > 0.22):
        normalized = 70.0
    elif (normalized <= 0.22 and normalized > 0.1):
        normalized = 80.0
    elif (normalized <= 0.1 and normalized > 0.025):
        normalized = 90.0
    else:
        normalized = 100.0
    wordDiff[word] = normalized

for filename in os.listdir("./gutenout"):
    if (not '.txt' in filename or filename == 'GLOBAL_counts.txt'):
        # ignore
        continue
    with open('./gutenout/' + filename) as f:
        localCounts = {}
        numWords = 0
        numUniqueWords = 0
        totalDiff = 0
        diffVec = []
        for line in f:
            arr = line.split("\t")
            word = arr[0]
            count = int(arr[1])
            localCounts[word] = count
            totalDiff += wordDiff[word] * count
            diffVec.append(wordDiff[word])
            numWords += count
            numUniqueWords += 1
        textId = filename[0:filename.find('_', 0, len(filename))]
        meanDiff = float(totalDiff) * 1.0/ numWords
        sumVec = [count * ((x - meanDiff)**2) for x in diffVec]
        stdevDiff = sqrt(float(sum(sumVec)) / numWords)
        
        # Get the book title
        title = getTitle(textId)

        # output the JSON
        with open('./json/' + textId + '.json', 'w') as out:
            print(json.dumps({'title': title, 'words':[{'difficulty_level': wordDiff[word], 'word': word, 'frequency': localCounts[word]} for word in localCounts], 'total_num_words': numWords, 'standard_deviation': stdevDiff, 'mean_word_difficulty': meanDiff}, ensure_ascii=False), file=out)

