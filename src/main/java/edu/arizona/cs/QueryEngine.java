package edu.arizona.cs;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;
/**
 * Class name: QueryEngine.java
 * @author ugola (Urvika Gola)
 * Assignment Number - 3
 * Instructor - Professor Mihai Surdeanu
 * TA names - Shreya Nupur Shakya
 * Due date - March 26th 2023
 * Inherits from: None
 * Interfaces: None
 *
 * Detailed Description - This program performs creates a Lucene Index for a given collection, and runs queries on this Index:
 * Task 1 Steps:   		It reads a given input.txt file (Collection of Documents with DocID)
 *                      Constructs the Index in ./index path
 *                      Reads the Query, Parses the Query to build a lucene Query
 *                      Runs the Query
 *                      Returns the Documents and Scores for the query
 *
 * Task 1: Input Requirement
 * The input.txt file containing the corpus
 *
 * Task 2 Steps:        Changes the default similarity formula (BM25) to consine-similarity (tf-idf) forumula
 *
 * Task 2: Input Requirement
 * The input.txt file containing the corpus
 *
 * Private Class Methods:
 * buildIndex()
 * cleanDirectory()
 * addDoc()
 * Public Class Methods:
 * parseQuery()
 * runQ1_1()
 * runQ1_2_a()
 * runQ1_2_b()
 * runQ1_2_c()
 * runQ1_3()
 **/
public class QueryEngine {
    boolean indexExists = false;									// If Index exists, it won't be rebuilt it
    String inputFilePath = "";
    StandardAnalyzer analyzer = new StandardAnalyzer();
    Directory index;
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    static final String INDEX_DIRECTORY = "./index";
    static int numberOfDocuments = 0;										// Stores the number of documents to be used for Hits Per Page
    /***
     * Constructor for Query Engine Class
     * @param inputFile the input.txt file name
     */
    public QueryEngine(String inputFile) {
        try {
            inputFilePath = inputFile;
            buildIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * Private method to build the index
     * @throws IOException
     */
    private void buildIndex() throws IOException {
        try {
            cleanDirectory(INDEX_DIRECTORY);						// Cleans the content of the Index Directory
            index = FSDirectory.open(Paths.get(INDEX_DIRECTORY));	// Uses File System Directory to store index in ./index
            IndexWriter w = new IndexWriter(index, config); 		// Build index at a given path using the configuration
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(inputFilePath).getFile());
            Scanner inputScanner = new Scanner(file);				// To read the contents of input.txt
            while (inputScanner.hasNextLine()) {
                String line = inputScanner.nextLine();
                String docIdSubstring = line.substring(0, line.indexOf(" ")).trim();	// Gets the document name e.g. Doc1, Doc100
                String textSubstring = line.substring(line.indexOf(" ") + 1, line.length()).trim();	// Gets the sentence post document name
                addDoc(w, textSubstring, docIdSubstring);								// Adds the parsed document
            }
            inputScanner.close();									// Closes the input scanner as the file is read
            w.close();												// Closes the index writer to release the lock
        } catch (IOException e) {
            e.printStackTrace();
        }
        indexExists = true;											// Successfully built the index
    }

    /***
     * Private method to clean the directory, when he program is run, the previous Index might still exist
     * @param indexDirectory the directory at which the Index is stored
     */
    private void cleanDirectory(String indexDirectory) {
        File directory = new File(indexDirectory);
        if (directory.exists() && directory.isDirectory()) {		// Checks if ./index exists
            File[] files = directory.listFiles();					// Get a list of all the files in the directory
            for (File file : files) {								// Iterate over the files
                file.delete();										// Delete each file in the directory
            }
	}}

    /***
     * Adds the document to the Index Writer
     * @param w - IndexWriter object
     * @param textSubstring - The searchable field of the document
     * @param docIdSubstring - The non-searchable document name
     * @throws IOException
     */
	private static void addDoc(IndexWriter w, String textSubstring, String docIdSubstring) {
        Document doc = new Document();
        doc.add(new TextField("title", textSubstring, Field.Store.YES));
        doc.add(new StringField("docid", docIdSubstring, Field.Store.YES));
        try {
			w.addDocument(doc);
			numberOfDocuments++;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /***
     * This is where the core logic lies.
     * @param queryStr - The parsed query coverted in lucene syntax
     * @param changeBM25Similarity - If set to false, the consine-similarity (tf-idf) forumula is set
     * @return Result Class list containing the results
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> parseQuery(String queryStr, boolean changeBM25Similarity) throws java.io.FileNotFoundException, java.io.IOException {
        try {
            Query q = new QueryParser("title", analyzer).parse(queryStr);	// Using same analyzer as the Index for searching purpose
            int hitsPerPage = numberOfDocuments;  							// Using the size of collection
            IndexReader reader = DirectoryReader.open(index);  				// Open the directory where index is stored
            IndexSearcher searcher = new IndexSearcher(reader);				// Read the index using IndexSearcher
            if (changeBM25Similarity) {										// Check if Similarity formula needs to be changed
                searcher.setSimilarity(new TFIDFSimilarity() {
                    @Override
                    public float tf(float v) {
                        return (float) Math.sqrt(v);
                    }

                    @Override
                    public float idf(long l, long l1) {
                        return (float) (Math.log(l1 / (l + 1)) + 1);
                    }

                    @Override
                    public float lengthNorm(int i) {
                        return (float) (1 / Math.sqrt(i));
                    }

                    @Override
                    public float sloppyFreq(int i) {
                        return 1 / ((float) i + 1);
                    }

                    @Override
                    public float scorePayload(int i, int i1, int i2, BytesRef bytesRef) {
                        return 1;
                    }
                });
            }
            TopDocs docs = searcher.search(q, hitsPerPage);					// Get the top documents returned by the IndexSearcher
            ScoreDoc[] hits = docs.scoreDocs;  								// Hits in the corresponding page
            List<ResultClass> docStoreList = new ArrayList<ResultClass>();
            String similarityUsed = changeBM25Similarity ? "Cosine" : "BM25";
            System.out.println(String.format("************** RESULTS for query '%s' using '%s' similarity ******************\n", queryStr, similarityUsed));
            for (ScoreDoc hit : hits) {
                int docId = hit.doc;				// Get the document ID in terms of 0, 1, 2 etc. (Lucene's internal implementation)
                Document d = searcher.doc(docId);	// Create the document object for this docID
                Document doc = new Document();		// Create a document object to add the objResultClass object
                doc.add(new TextField("title", d.get("title"), Field.Store.YES));	// Fetch the Title from the lightweight object
                doc.add(new StringField("docid", d.get("docid"), Field.Store.YES));	// Fetch the doc name e.g. Doc1, Doc100 from the lightweight object
                ResultClass objResultClass = new ResultClass();						// Stores the result
                objResultClass.DocName = doc;										// Add the document
                objResultClass.docScore = hit.score;								// Add the score
                System.out.println("DocID = " + doc.get("docid") + "; Score = " + hit.score + "\n");
                docStoreList.add(objResultClass);									// Update the result
            }
            return docStoreList;													// Return the result
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return new ArrayList<ResultClass>();										// Default value to return result
    }

	/***
	 * Creates an instance of the class and class a method
	 * @param args
	 */
    public static void main(String[] args ) {
        try {
            String fileName = "input.txt";									
            System.out.println("********Welcome to  Homework 3!");					
            String[] query13a = {"information", "retrieval"};				
            QueryEngine objQueryEngine = new QueryEngine(fileName);			
            objQueryEngine.runQ1_1(query13a);	// Get the result and print it
            objQueryEngine.runQ1_2_a(query13a);	// Get the result and print it
            objQueryEngine.runQ1_2_b(query13a);	// Get the result and print it
            objQueryEngine.runQ1_2_c(query13a);	// Get the result and print it
            objQueryEngine.runQ1_3(query13a);	// Get the result and print it

        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /***
     * Parses a query, prints list of documents with their name and score
     * @param query - 'information retrieval'
     * This uses the BM25 Similarity
     * @return ArrayList of ResultClass object
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_1(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if(!indexExists) {
            buildIndex();
        }
        String queryStr = String.join(" ", query); 	// Join information and query with a space to form "information retrieval"
        return parseQuery(queryStr, false);    }

    /***
     * Parses a query, prints list of documents with their name and score
     * This uses the BM25 Similarity
     * @param query 'information AND retrieval'
     * @return ArrayList of ResultClass object
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_2_a(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if(!indexExists) {
            buildIndex();
        }
        String queryStr = String.join(" AND ", query);
        return parseQuery(queryStr, false); // Print name and score
    }

    /***
     * Parses a query, prints list of documents with their name and score
     * This uses the BM25 Similarity
     * @param query 'information AND NOT retrieval'
     * @return ArrayList of ResultClass object
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_2_b(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if(!indexExists) {
            buildIndex();
        }
        String queryStr = String.join(" AND NOT ", query);
        return parseQuery(queryStr, false);
    }

    /***
     * Parses a query, prints list of documents with their name and score
     * This uses the BM25 Similarity
     * @param query 'information AND retrieval WITHIN 1 WORD OF EACH OTHER'
     * @return ArrayList of ResultClass object
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_2_c(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if(!indexExists) {
            buildIndex();
        }
        String queryStr = "\"" + String.join(" ", query) + "\"~1";
        return parseQuery(queryStr, false);
    }

    /***
     * Parses a query, prints list of documents with their name and score
     * This uses the Cosine Similarity
     * @param query 'information retrieval'
     * @return ArrayList of ResultClass object
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_3(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if(!indexExists) {
            buildIndex();
        }
        String queryStr = String.join(" ", query);
        return parseQuery(queryStr, true);
    }
}
