#include <iostream>
#include <fstream>
#include <string>
#include <bits/stdc++.h>
using namespace std;

/**
* This function takes an input data file and prepares a single level index on
* it. For this the data file is read line-by-line and in this process the key
* and the index of the record are stored in an array. Then the arrayList is
* bubble sorted and this sorted arrayList is written into a binary file.
*
* @param inputFileName
* @param outputFileName
* @param numberOfBytes as Key Length
* @return int 0 defines error 1 define success
*/
int createIndex(string inputFileName, string indexFileName, int keyLength) {

    fstream inputFile, indexFile;

    inputFile.open(inputFileName, ios::in|ios::binary);
    indexFile.open(indexFileName, ios::in|ios::out|ios::trunc|ios::binary);
    string data = "";
    char *key = (char *)malloc(sizeof(*key) * keyLength);
    char *key_s = (char *)malloc(sizeof(*key_s) * keyLength);
    long long offset = 0;
    const long long lengthIndex = keyLength + sizeof(offset);

    while (getline(inputFile, data)) {
        memcpy(key, &data[0], keyLength);
        indexFile.seekp(0, ios::end);
        int nRecord = indexFile.tellp() / lengthIndex;

        int i = 0;
        for (i = 0; i < nRecord; ++i) {
            indexFile.seekg(i * lengthIndex);
            indexFile.read(key_s, keyLength);
            if (strcmp(key, key_s) < 0)
                break;
        }

        long long posS;
        for (int j = i; j < nRecord; ++j) {
            long long p = j * lengthIndex;
            indexFile.seekg(p);

            indexFile.read(key_s, keyLength);
            indexFile.read(reinterpret_cast<char *>(&posS), sizeof(posS));

            indexFile.seekp(p);

            indexFile.write(reinterpret_cast<char *>(key), keyLength);
            indexFile.write(reinterpret_cast<char *>(&offset), sizeof(offset));

            memcpy(key, key_s, keyLength);
            memcpy(&offset, &posS, sizeof(offset));
        }
        indexFile.seekp(0, ios::end);
        indexFile.write(reinterpret_cast<char *>(key), keyLength);
        indexFile.write(reinterpret_cast<char *>(&offset), sizeof(offset));
        offset = inputFile.tellg();
    }
    inputFile.close();
    indexFile.close();
    free(key);
    free(key_s);

    return 1;
}

/**
* This function lists the records in the data file with respect to the order in
* the indexing file. For this, the position is read from the indexing file one
* after the other. For each position the corresponding record from the data file
* is read, based on this position, and displayed on the screen.
* @param inputFileName
* @param outputFileName
* @param numberOfBytes as Key Length
* @return int 0 defines error 1 define success
*/
int listIndex(string inputFileName, string indexFileName, int keyLength) {
    fstream indexFile;
    fstream inputFile;

    inputFile.open(inputFileName, ios::in|ios::binary);
    indexFile.open(indexFileName, ios::in|ios::binary);

    string data = "";
    char *key = (char *)malloc(sizeof(*key) * keyLength);
    long long offset = 0;
    indexFile.seekg(0, indexFile.end);
    long long len_file = indexFile.tellg();

    indexFile.seekg(ios::beg);
    const long long len_index = keyLength + sizeof(offset);
    int nRecord = len_file / len_index;
    for (int i = 0; i < nRecord; ++i) {
        indexFile.seekg(i * len_index);
        indexFile.read(key, keyLength);
        indexFile.read(reinterpret_cast<char *>(&offset), sizeof(offset));
        inputFile.seekg(offset);
        getline(inputFile, data);
        cout << data << endl;
    }

    inputFile.close();
    indexFile.close();
    free(key);

    return 1;
}

/***
 * Index program: This program is used to create a single level index on
 * a text file and storing it in the form of a binary file. This program can
 * also display the contents of the text file in accordance with the single
 * level index. The type of output is determined by the command line arguments
 * which include the name of the text file, the name of the binary file,the
 * number of bytes that will form the key and the type of output required. The
 * type of output can be -c which means to create an index or '–l' which
 * indicates that you should list the file using the index. Written by
 * Vedant Kumar NetID: vxk180003
 * @return int 0 defines error 1 define success
 */
int main(int argc, char *argv[])
{
    if(argc<5){
        cout << "Required number of inputs are absent." << "\n";
        return 0;
    }
    string operation = argv[1];
    if(operation!="-l" && operation!="-c"){
        cout << "Invalid input operation." << "\n";
        return 0;
    }
    string txt = ".txt";
    string inputFileName(argv[2]);
    ifstream inputFile;
    inputFile.open(inputFileName);
    if (inputFileName.find(txt) == std::string::npos || !inputFile.is_open() ) {
        cout << "Error opening data file / doesn't exist." << "\n";
        return 0;
    }
    if(inputFile.is_open()){
        inputFile.close();
    }
    string indexFileName(argv[3]);
    ifstream indexFile;
    indexFile.open(indexFileName);
    string idx = ".idx";
    if(indexFileName.find(idx) == std::string::npos){
        cout << "Invalid output file format." << "\n";
        return 0;
    }
    if(operation=="-l" && !indexFile.good()){
        cout << "Index file does not exist. Use -c to create the index file." << "\n";
        return 0;
    }
    if(indexFile.is_open()){
        indexFile.close();
    }
    int keyLength = stoi(argv[4]);
    if(keyLength<1 || keyLength>24){
        cout << "Invalid number of bytes for key." << "\n";
        return 0;
    }
    if(operation=="-c"){
        createIndex(inputFileName, indexFileName, keyLength);
    }
    else if(operation=="-l"){
        listIndex(inputFileName, indexFileName, keyLength);
    }
    return 1;
}
