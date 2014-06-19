# CorporateRelationExtraction #

This repository contains code for extracting typed corporate relationships 
from press release documents as part of the Sloan corporate network extraction
project.  This file gives an overview of the organization of the
code, how to run the various components, and some notes about possible 
future improvements.  You can get an idea of how things work by reading 
through the other Sloan documentation included in the Sloan project 
distribution (in the Sloan tarball), reading through
this document, and then reading through the description of each class
located at the top of each code file.  A lot of the documentation within
the code refers to the tech report, so it's important that you read over
that document before doing anything else.

The Sloan tarball which contains the rest of the documents related to the
Sloan project is included in this repository under the *files* directory.
If you've just received the link to this repository, then you should start
by extracting the files and documentation from that tarball.  It contains
pointers to several over repositories that are part of the overarching project.

In general, the library Jars included in the tarball should be up-to-date except
for *ark-water.jar* which contains the ARKWater project which is actively
being developed.  When you're setting up the code for the Sloan projects, you
may need to check out and compile the most recent version of ARKWater (from 
https://github.com/forkunited/ARKWater).  This should get rid of any errors
or warnings that come up.

## Layout of the project ##

The code is organized into the following packages in the *src* directory:

*	*corp.data* - Classes for loading corporation meta-data, gazetteers,
LDA output, and other resources into memory. 

*	*corp.data.annotation* - Classes for loading annotated press release
documents and Stanford pipeline output into memory.

*	*corp.data.feature* - Classes for computing feature values from annotated 
press release documents.

*	*corp.experiment* - Classes for running experiments that train and 
evaluate the relation extraction model, containing the main functions where the
experiments start running.  The experiments are deserialized from
the configuration files in the *experiments* directory.  

*	*corp.model* - Classes representing the relation extraction model. 

*	*corp.model.evaluation* - Classes used by *corp.experiment* to evaluate
the model.

*	*corp.scratch* - Code for performing miscellaneous tasks.  The files in
this directory contain the main functions where the code starts running.

*	*corp.test* - Code for unit testing.  Most of the code hasn't been  
systematically tested, so this is practically empty.

*	*corp.util* - Miscellaneous utility classes.

*corp.scratch* and *corp.experiment* contain the entry points for the code,
but *corp.scratch* just performs miscellaneous tasks.  So if you're trying
to understand how this library is used to train and test the extraction
models, you should start by looking at classes in *corp.experiment*.

The *experiments* directory contains experiment configuration files for 
running experiments using *corp.experiment* classes.  The 
*experiments/ablation* sub-directory contains "ablation studies" 
experiments, the "experiments/test" directory contains experiments 
for testing the final model on the test data set, and the remaining 
sub-directories contain experiments that were run to choose features
for the various logistic regressions at different labels in the 
taxonomy tree model (see the Sloan technical report for more information).

## How to run things ##

Before running anything, you need to configure the project for your local 
setup.  To configure, do the following:

1.  Copy files/build.xml and files/corp.properties to the top-level directory
of the project. 

2.  Fill out the copied corp.properties and build.xml files with the 
appropriate settings by replacing text surrounded by square brackets.

## Possible Improvements ##

Here are some things to be wary of, and which you might want to consider 
improving:

* The code is generally not very memory efficient because we were initially 
working with a very small data set when training and testing the model.  This 
is especially true of the featurization code in *corp.data.feature*. So things 
might break when you try training models on large amounts of data.  There
are many easy ways to try to fix this (don't keep feature name strings in memory,
store sparse representations, caching, etc).

* The model outputs featurized data to a text file before running logistic 
regression (see *corp.model.ModelCReg*).  Currently, all feature values are
output, but the logistic regression can take a sparse representation.  So it
would be better not to output zero-valued features. 

* The featurization code featurizes the output from the Stanford CoreNLP 
pipeline.  Currently, we have two sets of documents output by the pipeline:
one set whose output includes parsing and coref, and one set that's limited to 
tokenization and NER.  The corporate relation annotated training data from
Brian can only be used with the set of documents that includes parsing 
and coref, and so you must use this set when training the model.  This is
due to the fact that Brian's training data was constructed based on the
sentence indices in the parsing/coref set, and these indices are different
from the indices in the NER-only set (due to changes in the code that
runs the Stanford pipeline between generating these two sets).  So, when 
you're training the models or running any experiments using corporate 
relation annotations, you have to use the version of the Stanford pipeline 
documents that includes parsing and coref.  On the other hand, if you are
just running the model over data to compute relationship posteriors (or
doing anything that doesn't require the annotated relationship data), then
you should use the NER-only set since its documents are significantly 
smaller and will require less time and space to process.  See Lingpeng
for more details about this.

* *corp.test* was intended to include unit tests, but it's essentially empty
now because we didn't do much formal testing.  You might want to develop
some tests in the future to make sure things are alright.

* The corp.scratch.RunModelTree class uses a trained relationship extraction
model to compute distributions over relationship types for the entire 
corpus of press release documents.  In the past, this was done using
Trestles (one the XSEDE super-computer thingermajigs), but it's probably more 
efficient to develop a version of this that runs on the Hadoop cluster. 
