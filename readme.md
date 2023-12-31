# Information Retrieval System. Search engine

## Built With

- Java 21 <img src="https://cdn.jsdelivr.net/npm/programming-languages-logos/src/java/java.png" style="height: 1rem">
- AtomicReferenceArray
- ExecutorService
- StringTemplate ➕
- Junit5
- Lombok 🏝️
- JFreeChart 📈
- Slf4j, Logback
- Maven

## Installation Requirements

This project requires the following dependencies to be installed on your machine:

1. **Java 21**
    - Download and install Java 21 from [Oracle's website](https://www.oracle.com/cis/java/technologies/downloads/#jdk21-linux).

2. **Apache Maven 3.9.4+**
    - Install Apache Maven version 3.9.4 or higher. Instructions can be found on the [official Apache Maven installation guide](https://maven.apache.org/install.html).

## Dataset
The project utilizes the [Large Movie Review Dataset](https://ai.stanford.edu/~amaas/data/sentiment/) from Stanford University. You can download the dataset from the provided link.

## Install

- You need to install [Java 21](https://www.oracle.com/cis/java/technologies/downloads/#jdk21-linux) on your machine
- Also, you need to have [Apache Maven 3.9.4+](https://maven.apache.org/install.html)
- [Large Movie Review Dataset](https://ai.stanford.edu/~amaas/data/sentiment/)

1. Clone the repository::

<pre>
<code>git clone https://github.com/sterniichuk/search-engine</code>
</pre>

2. Navigate into the 'search-engine' folder:

<pre>
<code>cd search-engine</code>
</pre>

3. Build the project:

- Provide the absolute path to the 'aclImdb_v1' folder.
<br>The app will automatically handle dataset splitting based on the variant passed as an argument.
- This command will also run tests where the dataset is required.
<pre>
<code>
mvn package -Dindexdataset=C:\Users\stern\Documents\aclImdb_v1
</code>
</pre>
<b>OR</b><br>
3. Run this command to get the JAR file without testing:
<pre>
<code>
mvn package -DskipTests
</code>
</pre>

## Run

- Provide the absolute path to the 'aclImdb_v1' folder. <br> The app will automatically split the dataset based on the variant you provide as an argument.
- Also, provide a path to a folder where you want to store statistics like .csv files and charts.
<pre>
<code>
java -jar --enable-preview .\runner\target\runner-1.0-SNAPSHOT.jar --source C:\Users\stern\Documents\aclImdb_v1 --output C:\Users\stern\Desktop\your-best-output-folder
</code>
</pre>

<table>
<tr>
    <td>Argument</td>
    <td>Default</td>
    <td>Description</td>
</tr>
<tr>
    <td>--source</td>
    <td>System.getProperty("user.dir")</td>
    <td>Path to <b>aclImdb_v1</b> folder. C:\some-path\aclImdb_v1</td>
</tr>
<tr>
    <td>--variant</td>
    <td>24</td>
    <td>Dataset split used for indexing.
<br> Use <b>-1</b>  to index the <b>whole</b> dataset</td>
</tr>
<tr>
    <td>--clients</td>
    <td>32</td>
    <td>Number of client <b>processes</b> making search requests to the server.
    <br> Clients only make requests when the <b>--mode</b> is set to <b>FULL</b>.
    </td>
</tr>
<tr>
    <td>--queries</td>
    <td>50</td>
    <td>Number of random queries asked by each client.
    <br>Each search phrase is randomly generated from files specific to this <b>variant</b></td>
</tr>
<tr>
    <td>--mode</td>
    <td>BUILDING</td>
    <td>Two modes are available:
    <ul>
        <li>
            FULL - index building and running client processes, 
        </li>
        <li>
            BUILDING - only index building
        </li>
    </ul>
</td>
</tr>
<tr>
    <td>--iterations</td>
    <td>1</td>
    <td>After each iteration, you will receive a chart showcasing the building time for each thread
    <br> Each iteration involves constructing an index for a specified list of <b>--threads</b>.
</td>
</tr>
<tr>
    <td>--output</td>
    <td>System.getProperty("user.dir")</td>
    <td>Directory where all statistic will be stored</td>
</tr>
<tr>
    <td>--threads</td>
    <td>1,4,8,16,32,64</td>
    <td>Comma-separated list of threads the program will run for each iteration</td>
</tr>
</table>