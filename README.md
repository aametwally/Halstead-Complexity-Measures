# Calculate Halstead Complexity Measures

This project is intended to calculate the Halstead complexity measures using the ASTParser. Halstead Complexity measures are software metrics introduced by Maurice Howard Halstead in 1977. These metrics are computed statically, without program execution. More information can be found on the wikipedia page: (https://en.wikipedia.org/wiki/Halstead_complexity_measures).


##Calculation
First, we need to compute the following numbers, given the program source code:

**n1** = the number of distinct operators  
**n2** = the number of distinct operands  
**N1** = the total number of operators  
**N2** = the total number of operands  

From these numbers, eight measures can be calculated:  

**Program vocabulary:** n = n1 + n2  
**Program length:** N = N1 + N2  
**Calculated program length:** N'=n1*log2(n1)+n2*log2(n2)  
**Volume:** V= N*log2(n)    
**Difficulty:** D=  (n1/2)  * (N2/n2)    
**Effort:** E= D*V  
**Time required to program:** T= E/18 seconds  
**Number of delivered bugs:** B=V/3000  



## Getting Started

### Prerequisities
* JDK 8 and JRE 8 to be installed on the machine.
* Gradle to be installed on the machine.


### Installing

Clone the project to your local repository:
```
git clone https://ametwally@bitbucket.org/ametwally/hw1_ahmedmetwally.git
```


Navigate to the project's main directory, then build the project using gradle 
```
gradle build
```


To execute the program from command line:
```
gradle execute
```
Then the program should ask you to enter the directory absolute path that you wish to calculate the halstead complexity measures for. Once you provide a valid directory absolute path, the program should return the 8 metrics of healsted complexity measures. 



### Example
There are two datasets in test_datasets directory that can be used to test the program. It is also possible to download any java program and test the program on it.

For example, download the Protein Family Alignment Annotation Tool (PFAAT) from sourcefodge (https://sourceforge.net/projects/pfaat/?source=typ_redirect)
```
wget https://sourceforge.net/projects/pfaat/files/OldFiles/pfaat-1_0-src.zip
```

Decompress the zipped file:
```
unzip pfaat-1_0-src.zip
```

Execute the CalcHalsteadMetrics program by:
```
build execute
```

Then provide the program with the absolute path of the unzipped PFAAT directory 




## Running the tests

There are a couple of test cases implemented in this program. These test cases ensure that every method works as expected. You can test them using:
```
gradle test
```




### Implementation Notes:
* I considered the 37 operators only in my implementation: =,>,<,!,~,?,->,==,>=,<=,!=,&&,||,++,--,+,-,\*,/,&,|,^,%,<<,>>,>>>,+=,-=,*=,/=,&=,|=,^=,%=,<<=,>>=,>>>=
* I haven't tested for LambdaExpression as it is added in JLS8 API, and I am using JLS3
* operator ":" captured in the switch and short version of for loop and if statements. 
* negative numbers are counted as one operator "-" operator and a literal number operand. 
* The program counts different parts of the import statements as operands.
* In case of many java files exists in the directory. The overall number of the distinct operators in the whole application is the sum of the distinct number of operators in each file. So, if we have the same operator in two files, we will have two distinct operators in the whole application. The same for the operands because of different scopes.
* In my implementation, I am checking if the parsed java code to the ASTparser has any syntax error or not. Sometime, with newer versions, the code return with a syntax error. At that case, The Calculate Halstead Complexity quits with an error.