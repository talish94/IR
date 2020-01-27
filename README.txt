##########################################
My Search Engine - A Search Engine Project
##########################################

This is the first part of the Search Engine Project which is part of the Information Retrieval Course.
Through this application, you can create an inverted index - posting files and dictionaries for a given corpus.

## Setup
The application was written in Java, so make sure Java is installed on your computer.

In order to run the application, you will have to download the files "mysearchengine.jar", "json-20160810.jar", make /////////////////!!!
sure the file "json-20160810.jar" is in the same folder as the file "MySearchEngine.jar", and double click
on "MySearchEngine.jar".

### There are 2 "Browse" buttons that open up a File Chooser window:
1. The first "Browse" button is for getting the path of the folder that contains the folder of the corpus
   and within it the stop words file. It's important that the corpuse's
   folder will be named "corpus", and that the stop words
   file will be named "stop_words.text" Otherwise the application will not run.
2. The second "Browse" button is for choosing the folder in which all the 
   posting files and dictionaries will be saved in.
   
You can also manually write the path of the files in the text box next to each "Browse" button.

### The "Stemming" check box:
While checked, it will do one of two things:
1. If the "Start" button will be pressed, it will alert the system that the indexing
   process will not be using stemming.
2. If the "Load Dictionary" button will be pressed, it will alert the system that the
   posting files and dictionaries that are loaded didn't went through stemming (and saved
   them accordingly).
   
While unchecked, it will do one of two things:
1. If the "Start" button will be pressed, it will alert the system that the indexing
   process will be using stemming.
2. If the "Load Dictionary" button will be pressed, it will alert the system that the
   posting files and dictionaries that are loaded went through stemming.

### The "Start" button:
The "Start" button starts the indexing process, with or without stemming (depends if
the "Stemming" checkbox is checked or unchecked), using the corpus given as input, and
writes the posting files and dictionaries to the posting path given as input.

Without using the two "Browse" buttons (or text boxes) to choose the necessary paths, the
"Start" button will not work.

This process will usually take 20 to 25 minutes for a corpus with about half a million
documents, depends on whether you check "Stemming" or not. It will pop up an alert when
it's done with the time the whole process took, the number of documents that were indexed, 
and the amount of unique terms that were indexed.

If one of the paths doesn't exist, or the path for the corpus doesn't include the corpus
directory or the stop words file inside it, an appropriate error alert will be shown.

### The "Load Dictionary" button:
The "Load Dictionary" button opens up a file chooser window. You should choose the parent
directory of the directory which all the posting and dictionary files are saved in. After 
selecting a right directory, it starts the loading process, with or without stemming (depends
if the "Stemming" checkbox was checked or not).

This process will take less than 10 to 40 seconds for a corpus with about half a million /////////////////////////// time /????
documents, depends on whether you checked "Stemming" or not. It will pop up an alert when
it's done.

If the path doesn't exist, or the directory in the path doesn't include all the necessary
files, an appropriate error alert will be shown.

For using the "Load Dictionary" button without any problem, the folder you put as input 
needs to include the matching file:
	postingFilesWithoutStemming OR postingFilesWithStemming (depends whether "Stemming"
   is checked or not).

### The "Reset" button: 
The "Reset" button will erase all the data that was saved (if any) in the disk under the posting
path, and all the data that was saved in the main memory as well (all the dictionaries).

### The "Show Dictionary" button:
The "Show Dictionary" button will open a new window with all of the unique terms in the
current dictionary (after indexing or loading), sorted by lexicographic order, with the number of times
each one of them was written in the corpus.

Keep in mind, the "Show dictionary" button will work only if the dataset was
loaded or indexed before. If you press the "Reset" button, the meaning is that the "Show Dictionary"
button will work again only after another loading or indexing.

### Addidtional data about the synchornization between "Activate" and "Load Dictionaries":
The application is able to handle any combinition of order between "Activate" and "Load
Dictionary" with or without stemming, given that all of that is done on the same corpus.
For example, you can load all the stemmed posting files of the corpus and after it press
"Activate" for the non-stemming option.

Keep in notice, that after Indexing or Loading in both options (Stemming / Not Stemming),
the application will not allow another Loading or Indexing.


## Files and directories structure:
After running the application, there will be some files and directories created on your
computer, under the path that was given as input in the lower "Browse" button or the
"Load Dictionary" button. We will now explain the structure of those files.

If you were loading or indexing a corpus with stemming, a directory named "postingFilesWithStemming" ///////////////////// ???????????????
will be created, and under it 27 files:
1. 26 text files for each letter in the ABC - which is the posting file for all the terms in the dictionary (stemmed).
2. "NUM.txt" - which is a posting file for all the other terms (number, %, prices, dates..)

If you were loading or indexing a corpus without stemming, a directory named "postingFilesWithoutStemming" /////////// ????????????
will be created, and under it 27 files:
1. 26 text files for each letter in the ABC - which is the posting file for all the terms in the dictionary (stemmed).
2. "NUM.txt" - which is a posting file for all the other terms (number, %, prices, dates..)

					   
## Link to the project's repository in github:
https://github.com/omerrauchbach/SearchEngine

## Outsource code used:
The Porter's Stemmer: https://tartarus.org/martin/PorterStemmer/java.txt

## Built With
JavaFX

##########################################

Authors

- Tali Schvartz
- Omer Rauchbach

##########################################
