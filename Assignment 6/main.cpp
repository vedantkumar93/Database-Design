//============================================================================
// Name        : main.cpp
// Author      : vedant
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <fstream>
#include <cstring>
#include <stack>
#include <cmath>
using namespace std;

/**
 * Key class - to store keys. Every Key has a key value and a pointer which is a tree
 * pointer if it is an internal entry or is a data pointer if it is a leaf entry.
 */
class Key{
    public:
    char *value;
    long long pointer;
};

/**
 * Entry class - To store each entry in B+ tree.
 * Determine type of entry leaf or internal.
 * Number of pointers in the entry.
 * Array of keys their respective pointers
 * next pointer to the next leaf entry if it is a leaf
 * entry or to the subtree containing the keys greater than
 * the last key present in the last key of the entry.
 */
 class Entry{
    public:
    char entryType;
    int numberOfKeys;
    Key *keys;
    long long nextPointer = -1;
    void initialize(fstream &indexFile, long long currentEntryPointer, int keyLength, int maximumNumberOfKeys) {
            char *key = (char *)malloc(sizeof(*key) * keyLength);
            long long pointer;
            indexFile.seekg(currentEntryPointer);
            indexFile.read(reinterpret_cast<char *>(&entryType), sizeof(entryType));
            indexFile.read(reinterpret_cast<char *>(&numberOfKeys), sizeof(numberOfKeys));
            keys = new Key[maximumNumberOfKeys+1];
            for(int i=0; i<numberOfKeys; i++){
                indexFile.read(key, keyLength);
                indexFile.read(reinterpret_cast<char *>(&pointer), sizeof(pointer));
                keys[i].value = (char *) malloc(keyLength);
                memcpy(keys[i].value, key, keyLength);
                keys[i].pointer = pointer;
            }
            if(entryType=='I'){
                indexFile.read(reinterpret_cast<char *>(&pointer), sizeof(pointer));
                if(numberOfKeys==maximumNumberOfKeys){
                    nextPointer = pointer;
                }
                else{
                    string blank = "";
                    keys[numberOfKeys].value = (char *)blank.c_str();
                    keys[numberOfKeys].pointer=pointer;
                }
            }
            else if(entryType=='L'){
                indexFile.read(reinterpret_cast<char *>(&pointer), sizeof(pointer));
                nextPointer = pointer;
            }
    }
 };

/**
* This function inserts the key in the internal entry in the correct position.
*/
Entry insertKeyInInternalEntry(Entry currentEntry,Key newKey,int maximumNumberOfKeys,long long nextEntryPointer) {
    Key *tempKeyArray = new Key[maximumNumberOfKeys+1];
	int currentKeyPosition = 0;
	string blank = "";
	while (currentKeyPosition < currentEntry.numberOfKeys
			&& strcmp(currentEntry.keys[currentKeyPosition].value, newKey.value) < 0) {
		currentKeyPosition++;
	}
	int tempPosition = 0;
	for (int j = currentKeyPosition; j < currentEntry.numberOfKeys; j++) {
		tempKeyArray[tempPosition] = currentEntry.keys[j];
		tempPosition++;
	}
	//Copying the last pointer
	if(currentEntry.numberOfKeys == maximumNumberOfKeys) {
		tempKeyArray[tempPosition].value = (char *)blank.c_str();
		tempKeyArray[tempPosition].pointer = currentEntry.nextPointer;
	}else {
		tempKeyArray[tempPosition] = currentEntry.keys[currentEntry.numberOfKeys];
	}
	currentEntry.keys[currentKeyPosition] = newKey;
	currentEntry.numberOfKeys++;
	tempPosition = 0;
	for (int j = currentKeyPosition + 1; j < currentEntry.numberOfKeys; j++) {
		currentEntry.keys[j] = tempKeyArray[tempPosition];
		tempPosition++;
	}
	if(currentEntry.numberOfKeys >= (maximumNumberOfKeys)) {
		currentEntry.nextPointer = tempKeyArray[tempPosition].pointer;
	}
	else {
		currentEntry.keys[currentEntry.numberOfKeys]= tempKeyArray[tempPosition];
	}

	if((currentKeyPosition+1) == maximumNumberOfKeys) {
		currentEntry.nextPointer = nextEntryPointer;
	}
	else if((currentKeyPosition+1) == currentEntry.numberOfKeys) {
		currentEntry.keys[currentKeyPosition+1].value = (char *)blank.c_str();
		currentEntry.keys[currentKeyPosition+1].pointer = nextEntryPointer;
	}else {
		currentEntry.keys[currentKeyPosition+1].pointer=nextEntryPointer;
	}
	return currentEntry;
}

/**
* This function inserts the key in the leaf entry in the correct position.
*/
Entry insertKeyInLeafEntry(Entry currentEntry, Key newKey, int maximumNumberOfKeys) {
    Key *tempKeyArray = new Key[maximumNumberOfKeys+1];
	int currentKeyPosition = 0;
	while (currentKeyPosition < currentEntry.numberOfKeys
			&& strcmp(currentEntry.keys[currentKeyPosition].value,newKey.value) < 0) {
			currentKeyPosition++;
		}
    int tempPosition = 0;
	for (int j = currentKeyPosition; j < currentEntry.numberOfKeys; j++) {
        tempKeyArray[tempPosition] = currentEntry.keys[j];
		tempPosition++;
    }
    currentEntry.keys[currentKeyPosition] = newKey;
    currentEntry.numberOfKeys++;
	tempPosition = 0;
    for (int j = currentKeyPosition + 1; j < currentEntry.numberOfKeys; j++) {
		currentEntry.keys[j] = tempKeyArray[tempPosition];
		tempPosition++;
	}
	return currentEntry;
}



/**
* This function writes the internal entry into the index file.
*/
void writeInternalEntryIntoIndexFile(long long currentEntryPointer, Entry currentEntry,
                                    fstream &indexFile,int maximumNumberOfKeys, int keyLength) {
	indexFile.seekp(currentEntryPointer);     // check seekg or seekp
	indexFile.write(reinterpret_cast<char *>(&(currentEntry.entryType)), sizeof(currentEntry.entryType));
	indexFile.write(reinterpret_cast<char *>(&(currentEntry.numberOfKeys)), sizeof(currentEntry.numberOfKeys));
	for (int i = 0; i < currentEntry.numberOfKeys; i++) {
        indexFile.write(reinterpret_cast<char *>(currentEntry.keys[i].value), keyLength);
		indexFile.write(reinterpret_cast<char *>(&(currentEntry.keys[i].pointer)), sizeof(currentEntry.keys[i].pointer));
	}
	if(currentEntry.numberOfKeys == maximumNumberOfKeys) {
	    indexFile.write(reinterpret_cast<char *>(&(currentEntry.nextPointer)), sizeof(currentEntry.nextPointer));
	}
	else {
		indexFile.write(reinterpret_cast<char *>(&(currentEntry.keys[currentEntry.numberOfKeys].pointer)),
                  sizeof(currentEntry.keys[currentEntry.numberOfKeys].pointer));
	}
}

/**
* This function changes the root entry pointer in the index file.
*/
void changeRootEntryPointer(long long newRootEntryPointer, fstream &indexFile){
    indexFile.seekp(260);
    indexFile.write(reinterpret_cast<char *>(&newRootEntryPointer), sizeof(newRootEntryPointer));
}

/**
* This function gets a pointer to the next available space for a new entry.
*/
long long getNextEntrySpace(fstream &indexFile) {
    indexFile.seekg(0, indexFile.end);
    long long lengthFile = indexFile.tellg();
    indexFile.seekg(ios::beg);

    long long nextEntryPointer = -1;
	double fileLength = (double) lengthFile;
	nextEntryPointer = (long long) ceil(fileLength / 1024) * 1024;
	return nextEntryPointer;
}

/**
* This function writes the leaf entry into the index file.
*/
void writeLeafEntryIntoIndexFile(long long currentEntryPointer, Entry currentEntry, fstream &indexFile, int keyLength) {
    indexFile.seekp(currentEntryPointer);
    indexFile.write(reinterpret_cast<char *>(&(currentEntry.entryType)), sizeof(currentEntry.entryType));
    indexFile.write(reinterpret_cast<char *>(&(currentEntry.numberOfKeys)), sizeof(currentEntry.numberOfKeys));
	for (int i = 0; i < currentEntry.numberOfKeys; i++) {
		indexFile.write(reinterpret_cast<char *>(currentEntry.keys[i].value), keyLength);
		indexFile.write(reinterpret_cast<char *>(&(currentEntry.keys[i].pointer)), sizeof(currentEntry.keys[i].pointer));
	}
	indexFile.write(reinterpret_cast<char *>(&(currentEntry.nextPointer)), sizeof(currentEntry.nextPointer));
}

/**
* This function inserts the records into the index file into the B+ tree data structure.
*/
void insertRecordIntoIndex(long long rootEntryPointer, fstream &indexFile, long long dataPointer,
                           char* key, int keyLength, int maximumNumberOfKeys){
    long long currentEntryPointer = rootEntryPointer;
    Entry currentEntry;
    string blank = "";
    stack <long long> entryTraversed;
    indexFile.seekg(rootEntryPointer);
    currentEntry.initialize(indexFile, currentEntryPointer, keyLength, maximumNumberOfKeys);
    while(currentEntry.entryType == 'I'){
        entryTraversed.push(currentEntryPointer);
        if(strcmp(key, currentEntry.keys[0].value)<=0){
            currentEntryPointer = currentEntry.keys[0].pointer;
        }
        else if(strcmp(key, currentEntry.keys[currentEntry.numberOfKeys-1].value)>0){
            if(currentEntry.numberOfKeys == maximumNumberOfKeys){
                currentEntryPointer = currentEntry.nextPointer;
            }else{
                currentEntryPointer = currentEntry.keys[currentEntry.numberOfKeys].pointer;
            }
        } else {
            for(int i=1; i<currentEntry.numberOfKeys; i++){
                if(strcmp(key, currentEntry.keys[i-1].value)>0 && (strcmp(key, currentEntry.keys[i].value)<=0)){
                    currentEntryPointer = currentEntry.keys[i].pointer;
                    break;
                }
            }
        }
        currentEntry.initialize(indexFile, currentEntryPointer, keyLength, maximumNumberOfKeys);
    }
    for(int i=0; i<currentEntry.numberOfKeys; i++){
        if(strcmp(currentEntry.keys[i].value, key)==0){
            cout << "Duplicate key " << key << " found." << "\n";
            return;
        }
    }
    // find the right position for the key in the given entry
    Key newKey;
    newKey.value = key;
    newKey.pointer = dataPointer;
    if(currentEntry.numberOfKeys != maximumNumberOfKeys){
            // if the current entry is not full then find the right position for the given key and place it
        currentEntry = insertKeyInLeafEntry(currentEntry, newKey, maximumNumberOfKeys);
        writeLeafEntryIntoIndexFile(currentEntryPointer, currentEntry, indexFile, keyLength);
    }
    else{
        //if the entry is full then split the entry into two child entrys and push the entry in between to the top level
        Entry tempEntry;
        tempEntry.entryType = currentEntry.entryType;
        tempEntry.numberOfKeys = currentEntry.numberOfKeys;
        tempEntry.nextPointer = currentEntry.nextPointer;
        tempEntry.keys = new Key[maximumNumberOfKeys+1];
        for(int i=0; i<currentEntry.numberOfKeys; i++){
            tempEntry.keys[i].pointer = currentEntry.keys[i].pointer;
            tempEntry.keys[i].value = currentEntry.keys[i].value;
        }
        tempEntry = insertKeyInLeafEntry(tempEntry, newKey, maximumNumberOfKeys);
        Entry newLeafEntry;
        newLeafEntry.entryType = 'L';
        newLeafEntry.nextPointer = currentEntry.nextPointer;
        newLeafEntry.keys = new Key[maximumNumberOfKeys];
        currentEntry.keys = new Key[maximumNumberOfKeys];
        long long newLeafEntryPointer = getNextEntrySpace(indexFile);
        int midPosition = (int) ceil(((double)(tempEntry.numberOfKeys+1))/2);
        int i;
        int j=0;
        for(i=0; i<midPosition; i++){
            currentEntry.keys[i]=tempEntry.keys[i];
        }
        currentEntry.numberOfKeys = midPosition;
        currentEntry.nextPointer = newLeafEntryPointer;
        for(i=midPosition; i<tempEntry.numberOfKeys; i++){
            newLeafEntry.keys[j] = tempEntry.keys[i];
            j++;
        }
        newLeafEntry.numberOfKeys = j;
        writeLeafEntryIntoIndexFile(currentEntryPointer, currentEntry, indexFile, keyLength);
        writeLeafEntryIntoIndexFile(newLeafEntryPointer, newLeafEntry, indexFile, keyLength);
        newKey.value = tempEntry.keys[midPosition-1].value;
        newKey.pointer = currentEntryPointer;
        long long nextEntryPointer = newLeafEntryPointer;
        bool finished = false;
        while(!finished){
            if(entryTraversed.empty()){
                Entry root;
                root.entryType = 'I';
                root.numberOfKeys = 1;
                root.keys = new Key[maximumNumberOfKeys];
                root.keys[0].value =newKey.value;
                root.keys[0].pointer = newKey.pointer;
                root.keys[1].value = (char *)blank.c_str();;
                root.keys[1].pointer = nextEntryPointer;
                long long newRootEntryPointer = getNextEntrySpace(indexFile);
                writeInternalEntryIntoIndexFile(newRootEntryPointer, root, indexFile, maximumNumberOfKeys, keyLength);
                changeRootEntryPointer(newRootEntryPointer, indexFile);
                finished = true;
            }
            else{
                currentEntryPointer = entryTraversed.top();
                entryTraversed.pop();
                Entry currentInternalEntry;
                currentInternalEntry.initialize(indexFile, currentEntryPointer, keyLength, maximumNumberOfKeys);
                if(currentInternalEntry.numberOfKeys!=maximumNumberOfKeys){
                    //if the internal entry is not full then insert the key in the correct position and end the loop
                    currentInternalEntry = insertKeyInInternalEntry(currentInternalEntry, newKey, maximumNumberOfKeys, nextEntryPointer);
                    writeInternalEntryIntoIndexFile(currentEntryPointer, currentInternalEntry, indexFile, maximumNumberOfKeys, keyLength);
                    finished = true;
                }
                else{ //if the given internal entry is full then split the given internal entry and propagate the entry to the next higher level
                    Entry tempInternalEntry;
                    tempInternalEntry.entryType = currentInternalEntry.entryType;
                    tempInternalEntry.numberOfKeys = currentInternalEntry.numberOfKeys;
                    tempInternalEntry.nextPointer = currentInternalEntry.nextPointer;
					tempInternalEntry.keys = new Key[maximumNumberOfKeys+1];
					for(i=0;i<=currentInternalEntry.numberOfKeys;i++) {
						tempInternalEntry.keys[i]=currentInternalEntry.keys[i];
					}
					tempInternalEntry = insertKeyInInternalEntry(tempInternalEntry,newKey,maximumNumberOfKeys,nextEntryPointer);
					Entry newInternalEntry;
					newInternalEntry.entryType = 'I';
                    newInternalEntry.keys = new Key[maximumNumberOfKeys];
					currentInternalEntry.keys = new Key[maximumNumberOfKeys];
					long long newInternalEntryPointer = getNextEntrySpace(indexFile);
					midPosition = (int) floor(((double)(tempInternalEntry.numberOfKeys+1))/2);
                    currentInternalEntry.numberOfKeys = midPosition;
                    for(i=0;i<midPosition;i++) {
						currentInternalEntry.keys[i].value = tempInternalEntry.keys[i].value;
						currentInternalEntry.keys[i].pointer = tempInternalEntry.keys[i].pointer;
					}
					currentInternalEntry.keys[midPosition].value = (char *)blank.c_str();;
					currentInternalEntry.keys[midPosition].pointer = tempInternalEntry.keys[midPosition].pointer;
                    j=0;
					for(i=midPosition+1;i<tempInternalEntry.numberOfKeys;i++) {
						newInternalEntry.keys[j].value = tempInternalEntry.keys[i].value;
						newInternalEntry.keys[j].pointer = tempInternalEntry.keys[i].pointer;
						j++;
					}
					newInternalEntry.numberOfKeys = j;
                    newInternalEntry.keys[j].value = (char *)blank.c_str();;
                    newInternalEntry.keys[j].pointer = tempInternalEntry.nextPointer;
					writeInternalEntryIntoIndexFile(currentEntryPointer, currentInternalEntry, indexFile,maximumNumberOfKeys, keyLength);
					writeInternalEntryIntoIndexFile(newInternalEntryPointer, newInternalEntry, indexFile,maximumNumberOfKeys, keyLength);
					newKey.value = tempInternalEntry.keys[midPosition].value;
					newKey.pointer = currentEntryPointer;
					nextEntryPointer = newInternalEntryPointer;

                }
            }
        }
    }
}

/**
* This function returns the root entry pointer.
*/
long long getRootEntryPointer(fstream &indexFile){
    long long pointer = -1;
    indexFile.seekg(260);
    indexFile.read(reinterpret_cast<char *>(&pointer), sizeof(pointer));
    return pointer;
}

/**
* This function is used to create an index on the given text file
*/
int createIndex(char *inputFileName, char *indexFileName, int keyLength) {
    fstream inputFile, indexFile;
    inputFile.open(inputFileName, ios::in|ios::binary);     // access the input text file
    indexFile.open(indexFileName, ios::in|ios::out|ios::trunc|ios::binary);

    /**
	 * Writing the header entry. The header entry consists of :
	 * 1. Input File Name in first 256 bytes i.e. 0 to 255
	 * 2. Count of pointers in a entry of the B+ tree
	 * 3. Pointer to the root entry. Initial this is 1024th byte 1k bytes are allocated for the header entry as well as each entry i.e.
	 * 1024 bytes 4.The key length.
    */
	indexFile.write(reinterpret_cast<char *>(inputFileName), strlen(inputFileName));   // writing the name of the text file into the header entry

	indexFile.seekp(256);
    /**
	* The meta data being stored for each entry is:
	* 1. Leaf entry (L) or internal entry(I) - 2 bytes
	* 2. Number of pointers occupied which is an integer - 4 bytes
	* Total = 6 bytes
	*/
	int metaDataLength = 6; // meta data length in bytes
    int maximumNumberOfKeys = (1024 - metaDataLength - 8) / (8 + keyLength);
	indexFile.write(reinterpret_cast<char *>(&maximumNumberOfKeys), sizeof(maximumNumberOfKeys));
	long long rootEntryPointer = 1024; // The first entry is after 1K bytes
    indexFile.write(reinterpret_cast<char *>(&rootEntryPointer), sizeof(rootEntryPointer));
	indexFile.write(reinterpret_cast<char *>(&keyLength), sizeof(keyLength));

    indexFile.seekp(rootEntryPointer);
    char l = 'L';
	indexFile.write(reinterpret_cast<char *>(&l), sizeof(l)); // Since the root entry is the only on present initially
    int temp1 = 1;
    indexFile.write(reinterpret_cast<char *>(&temp1), sizeof(temp1)); // Since the number of pointers present is 0
	long long dataPointer = inputFile.tellg();
	string data = "";
	getline(inputFile, data);
	char *key = (char *)malloc(sizeof(*key) * keyLength);
	memcpy(key, &data[0], keyLength);
    indexFile.write(reinterpret_cast<char *>(key), keyLength);
    indexFile.write(reinterpret_cast<char *>(&dataPointer), sizeof(dataPointer));
    long long temp2 = -1;
	indexFile.write(reinterpret_cast<char *>(&temp2), sizeof(temp2)); //The next pointer for the given leaf entry is null
	dataPointer = inputFile.tellg();
    while (getline(inputFile, data)) {
        memcpy(key, &data[0], keyLength);
        rootEntryPointer = getRootEntryPointer(indexFile);
        insertRecordIntoIndex(rootEntryPointer, indexFile, dataPointer, key, keyLength,
						maximumNumberOfKeys);
        dataPointer = inputFile.tellg();
    }
    free(key);
    inputFile.close();
    indexFile.close();
    return 1;
}

/**
 * find the record and print it to console for the given key using indexFile
 */
int findRecord(char* indexFileName, char* keyValue){
    fstream indexFile, inputFile;
    indexFile.open(indexFileName, ios::in | ios::binary);
    char *refByte = (char *)malloc(256);
    indexFile.seekg(0);
    indexFile.read(refByte, 256);
    inputFile.open(refByte, ios::in | ios::binary);

    int maximumNumberOfKeys;
    indexFile.read(reinterpret_cast<char *>(&maximumNumberOfKeys), sizeof(maximumNumberOfKeys));

    long long rootEntryPointer;
    indexFile.read(reinterpret_cast<char *>(&rootEntryPointer), sizeof(rootEntryPointer));

    int keyLength;
    indexFile.read(reinterpret_cast<char *>(&keyLength),sizeof(keyLength));

    long long currentEntryPointer = rootEntryPointer;

    Entry currentEntry;
    bool found = false;

    int keyPosition = -1;

    if(strlen(keyValue)>keyLength){
        string temp(keyValue);
        temp = temp.substr(0,keyLength);
        keyValue = (char*)temp.c_str();

    }else if(strlen(keyValue) < keyLength){
        int lengthDiff = keyLength - strlen(keyValue);
        string temp(keyValue);
        for(int i=0; i<lengthDiff; i++){
            temp = temp + " ";
        }
        keyValue = (char*)temp.c_str();
    }

    indexFile.seekg(rootEntryPointer);
    currentEntry.initialize(indexFile, currentEntryPointer, keyLength, maximumNumberOfKeys);
    while(currentEntry.entryType != 'L'){
        if(strcmp(keyValue,currentEntry.keys[0].value) <= 0){
            currentEntryPointer = currentEntry.keys[0].pointer;
        }else if(strcmp(keyValue,currentEntry.keys[currentEntry.numberOfKeys-1].value) >0){
            if(currentEntry.numberOfKeys == maximumNumberOfKeys){
                currentEntryPointer = currentEntry.nextPointer;
            }else{
                currentEntryPointer = currentEntry.keys[currentEntry.numberOfKeys].pointer;
            }
        }else{
            for(int i=1; i<currentEntry.numberOfKeys; i++){
                if((strcmp(keyValue,currentEntry.keys[i-1].value) > 0) && (strcmp(keyValue,currentEntry.keys[i].value) <=0)){
                    currentEntryPointer = currentEntry.keys[i].pointer;
                    break;
                }
            }
        }
        currentEntry.initialize(indexFile, currentEntryPointer, keyLength, maximumNumberOfKeys);
    }

    for (int i = 0; i < currentEntry.numberOfKeys; i++) {
        if (strcmp(currentEntry.keys[i].value,keyValue) == 0) {
            found = true;
            keyPosition = i;
            break;
        }
    }
    if(found) {
        long long recordPointer = currentEntry.keys[keyPosition].pointer;
        inputFile.seekg(recordPointer);
        string requiredRecord;
        getline(inputFile,requiredRecord);
        cout<<"At "<<recordPointer<<" the record is "<<requiredRecord;
    }else {
        cout<<"Record for the given key not found!";
    }
    inputFile.close();
    indexFile.close();

    return 1;
}

/**
* This function checks if the record with the given key already exists and if it does not then the record
* is inserted into the data file and then into the index file.
*/
int insertRecordIntoDataFile(char* indexFileName, char* newRecord){
    fstream indexFile, inputFile;
    indexFile.open(indexFileName, ios::in|ios::out|ios::binary);
	char* inputFileName = (char *)malloc(256); // reference byte to read keys from text file
	indexFile.seekg(0);
	indexFile.read(inputFileName, 256);
	inputFile.open(inputFileName, ios::in|ios::app);     // access the input text file
	int maximumNumberOfKeys;
	indexFile.read(reinterpret_cast<char *>(&maximumNumberOfKeys), sizeof(maximumNumberOfKeys));
	long long rootEntryPointer;
	indexFile.read(reinterpret_cast<char *>(&rootEntryPointer), sizeof(rootEntryPointer));
	int keyLength;
	indexFile.read(reinterpret_cast<char *>(&keyLength), sizeof(keyLength));
	long long currentEntryPointer = rootEntryPointer;
	Entry currentEntry;
	bool found = false;
	char* keyValue = (char *)malloc(keyLength);
	memcpy(keyValue, newRecord, keyLength);
	indexFile.seekg(rootEntryPointer);
	currentEntry.initialize(indexFile, currentEntryPointer, keyLength, maximumNumberOfKeys);
	while (currentEntry.entryType=='I') {
		if (strcmp(keyValue, currentEntry.keys[0].value) <= 0) {
			currentEntryPointer = currentEntry.keys[0].pointer;
		} else if (strcmp(keyValue,currentEntry.keys[currentEntry.numberOfKeys-1].value) > 0) {// if the key value is greater than the last key then point to the right most child of the entry
			if(currentEntry.numberOfKeys == maximumNumberOfKeys) {
				currentEntryPointer = currentEntry.nextPointer;
			}else {
				currentEntryPointer = currentEntry.keys[currentEntry.numberOfKeys].pointer;
			}
		} else { // find the pointer to the next level by comparing the keys
			for (int i = 1; i < currentEntry.numberOfKeys; i++) {
				if ((strcmp(keyValue, currentEntry.keys[i - 1].value) > 0)
						&& (strcmp(keyValue, currentEntry.keys[i].value) <= 0)) {
					currentEntryPointer = currentEntry.keys[i].pointer;
					break;
				}
			}
		}
		currentEntry.initialize(indexFile, currentEntryPointer, keyLength, maximumNumberOfKeys);
	// search the leaf entry for the key value
	}
	for (int i = 0; i < currentEntry.numberOfKeys; i++) {
		if (strcmp(currentEntry.keys[i].value,keyValue) == 0) { // if the key exists, then the record cannot be inserted
			found = true;
			break;
		}
	}
	if(found) {
		cout << "Record with the given key is already present.";
		return 0;
	}
	else { //if the record is not found , then insert it first into the data file and then into the index file
		inputFile.seekg(0, inputFile.end);
		long long dataFileLength = inputFile.tellg();
		char* newLine = "\n";
		inputFile.seekp(dataFileLength);
		inputFile.write(reinterpret_cast<char *>(newRecord), strlen(newRecord));
		inputFile.write(reinterpret_cast<char *>(newLine), strlen(newLine));
		cout << "New record successfully inserted at position " << dataFileLength << " in text file.";
		insertRecordIntoIndex(rootEntryPointer,indexFile,dataFileLength,keyValue,keyLength,maximumNumberOfKeys);
	}
	inputFile.close();
	indexFile.close();
    return 1;
}

/**
 * Print the records from data file for the given keyValue and its subsequent numberOfRecords
 */
int listSequentialRecords(char* indexedFileName, char* keyValue, int numberOfRecords){
    fstream indexedFile, inputFile;

    indexedFile.open(indexedFileName, ios::in | ios::binary);
    char *inputDataFile = (char *)malloc(256);
    indexedFile.seekg(0);
    indexedFile.read(inputDataFile, 256);
    inputFile.open(inputDataFile, ios::in | ios::binary);

    int maximumNumberOfKeys;
    indexedFile.read(reinterpret_cast<char *>(&maximumNumberOfKeys), sizeof(maximumNumberOfKeys));

    long long rootEntryPointer;
    indexedFile.read(reinterpret_cast<char *>(&rootEntryPointer), sizeof(rootEntryPointer));

    int keyLength;
    indexedFile.read(reinterpret_cast<char *>(&keyLength),sizeof(keyLength));

    long long currentEntryPointer = rootEntryPointer;

    Entry currentEntry;
    bool found = false;

    int keyPosition = -1;

    indexedFile.seekg(rootEntryPointer);
    currentEntry.initialize(indexedFile, currentEntryPointer, keyLength, maximumNumberOfKeys);
    while(currentEntry.entryType != 'L'){
        if(strcmp(keyValue,currentEntry.keys[0].value) <= 0){
            currentEntryPointer = currentEntry.keys[0].pointer;
        }else if(strcmp(keyValue,currentEntry.keys[currentEntry.numberOfKeys-1].value) >0){
            if(currentEntry.numberOfKeys == maximumNumberOfKeys){
                currentEntryPointer = currentEntry.nextPointer;
            }else{
                currentEntryPointer = currentEntry.keys[currentEntry.numberOfKeys].pointer;
            }
        }else{
            for(int i=1; i<currentEntry.numberOfKeys; i++){
                if((strcmp(keyValue,currentEntry.keys[i-1].value) > 0) && (strcmp(keyValue,currentEntry.keys[i].value) <=0)){
                    currentEntryPointer = currentEntry.keys[i].pointer;
                    break;
                }
            }
        }
        currentEntry.initialize(indexedFile, currentEntryPointer, keyLength, maximumNumberOfKeys);
    }

    for (int i = 0; i < currentEntry.numberOfKeys; i++) {
        if (strncmp(currentEntry.keys[i].value,keyValue,keyLength) == 0) {
            found = true;
            keyPosition = i;
            break;
        }
    }

    if(!found){
        //print if the record is present in the file
        cout<<"No record found for the given key. Searching another record for the key greater than the given key.";
        for(int i = 0; i < currentEntry.numberOfKeys; i++) {
            if (strcmp(currentEntry.keys[i].value,keyValue) > 0) { // break out of loop and store its position if the key exists
                found = true;
                keyPosition = i;
                break;
            }
            if(i == (currentEntry.numberOfKeys-1)) { //if the key greater than the given key is not present in the given entry, then switch to the next leaf entry
                if(currentEntry.nextPointer!=-1) {
                    currentEntry.initialize(indexedFile, currentEntry.nextPointer, keyLength, maximumNumberOfKeys);
                    i=0;
                }
            }
        }
    }
    if(!found) {
        cout<<"No Record for the key or a key greater than the given key";
        return 0;
    }else {
        for(int i=0;i<numberOfRecords;i++) {
            long long recordPointer = currentEntry.keys[keyPosition].pointer;
            inputFile.seekg(recordPointer);
            string requiredRecord = "";
            getline(inputFile, requiredRecord);
            cout<<"At "<<recordPointer<<" , record "<<requiredRecord<<"\n";
            if(keyPosition == (currentEntry.numberOfKeys-1)) { //if the key greater than the given key is not present in the given entry, then switch to the next leaf entry
                if(currentEntry.nextPointer!=-1) {
                    currentEntry.initialize(indexedFile, currentEntry.nextPointer, keyLength, maximumNumberOfKeys);
                    keyPosition=0;
                }else {
                    break;
                }
            }else {
                keyPosition++;
            }
        }
    }

    inputFile.close();
    indexedFile.close();

    return 1;
}

/***
 * MultiLevelIndexingApplication program This program is used to create a
 * multilevel index on a given data file using a B+ tree data structure.
 *
 * 4 operations can be specified by the user.
 *
 * Sample Input:
 *
 * -create CS6360Asg5TestDataB.txt CS6360Asg5TestDataB.indx 4
 * -insert CS6360Asg5TestDataB.indx "GGGG This is Cole from DBMS"
 * -list CS6360Asg5TestDataB.indx CCCC 3
 * -find CS6360Asg5TestDataB.indx CCCC
 */
int main(int argc, char *argv[])
{
    string operation = argv[1]; // The first argument is the operation
    if(operation=="-create"){
        if(argc!=5){
            cout << "Invalid Number of args passed." << "\n";
            return 0;
        }
        char *inputFileName = argv[2];  // The second argument is the input text file path/name
        char *indexFileName = argv[3];  // The third argument is the output index file path/name
        int keyLength = stoi(argv[4]);  // The fourth argument is the key length in bytes
        if(keyLength<1 || keyLength>40){
            cout << "Invalid count of bytes for key." << "\n";
            return 0;
        }
        createIndex(inputFileName, indexFileName, keyLength);  // calling the create index function
    }
    else if(operation=="-find"){
        if(argc!=4){
            cout << "Invalid Number of args passed." << "\n";
            return 0;
        }
        char* indexFileName = argv[2];
        char* keyValue = argv[3];
        findRecord(indexFileName, keyValue);
    }
    else if(operation=="-insert"){
        if(argc!=4){
            cout << "Invalid Number of args passed." << "\n";
            return 0;
        }
        char* indexFileName = argv[2];
        char* newRecord = argv[3];
        insertRecordIntoDataFile(indexFileName, newRecord);
    }
    else if(operation=="-list"){
        if(argc!=5){
            cout << "Invalid Number of args passed." << "\n";
            return 0;
        }
        char* indexFileName = argv[2];
        char* startingKey = argv[3];
        int numberOfRecords = stoi(argv[4]);
        listSequentialRecords(indexFileName, startingKey, numberOfRecords);
    }
    else{
        cout << "Invalid input operation." << "\n";
        return 0;
    }
    return 1;
}
