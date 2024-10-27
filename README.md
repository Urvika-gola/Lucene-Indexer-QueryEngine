# Lucene Document Indexer and QueryEngine

## Overview
This project implements a document indexing and search engine using **Apache Lucene**. The `QueryEngine` class allows users to build a Lucene index from a document corpus and perform complex search queries, with support for both BM25 and TF-IDF (Cosine Similarity) scoring.

Developed for **CSC483-583-SP23** at **University of Arizona** under **Professor Mihai Surdeanu**

## Features
- **Document Indexing**: Parses `input.txt`, builds an index, and saves it to `./index`.
- **Boolean & Proximity Queries**: Executes Boolean (`AND`, `OR`, `NOT`) and proximity queries.
- **Customizable Scoring**: Supports BM25 (default) and Cosine Similarity (TF-IDF) scoring.
- **Automatic Index Cleanup**: Cleans previous indexes to avoid duplicates.

## Query Processing
Queries are parsed and converted into Lucene syntax to support Boolean and proximity operations. The parseQuery method handles query execution based on user-specified scoring preferences.

## BM25 Scoring: Used by default for all queries.
Cosine Similarity: Enabled by setting the changeBM25Similarity flag to true.
Scoring Mechanism
The QueryEngine class uses BM25 as the default similarity function for relevance. For Cosine Similarity, Lucene’s TF-IDF scoring is applied, with custom functions to adjust term frequency, inverse document frequency, and normalization factors.

## Dependencies
The project relies on the following Apache Lucene modules:

lucene-core: Core Lucene indexing and search functionality.
lucene-analyzers-common: Includes standard text analyzers.
lucene-queryparser: Allows parsing of complex queries.

## File Structure
src/main/java/edu/arizona/cs/QueryEngine.java: Main Java class implementing the QueryEngine functionality.
input.txt: Sample corpus containing document IDs and text.
index: Directory where Lucene stores the indexed documents.
