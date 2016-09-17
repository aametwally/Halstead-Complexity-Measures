# Calculate Halstead Complexity Measures

This project is intended to calculate the Halstead complexity measures which are software metrics introduced by Maurice Howard Halstead in 1977. These metrics are computed statically, without program execution. More information can be found on the wikipedia page: (https://en.wikipedia.org/wiki/Halstead_complexity_measures)


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


Navigate to the project's main directory. Then build the project using gradle 
```
gradle build
```


To execute the program from command line:
```
gradle execute
```
Then the program should ask you to enter the directory path that you wich to calculate the halstead complexity measures for. Once you provide a valid directory path, the program should return the 8 metrics of healsted complexity measures. 


### Example
Download the Protein Family Alignment Annotation Tool (PFAAT) from sourcefodge (https://sourceforge.net/projects/pfaat/?source=typ_redirect)
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


Then give the path of the unzipped PFAAT directory 




## Running the tests

There are couple of tests implemented in this program. to test the program using the JUnit tests:

```
gradle test
```




### Implementation Notes:


* I considered the 37 operators only in my implementation: =,>,<,!,~,?,->,==,>=,<=,!=,&&,||,++,--,+,-,*,/,&,|,^,%,<<,>>,>>>,+=,-=,*=,/=,&=,|=,^=,%=,<<=,>>=,>>>=
* I haven't tested for LambdaExpression as it is added in JLS8 API, and I am using JLS3
* operator ":" captured in the switch and short version of for loop and if statements. 
* negative numbers are counted as one operator "-" operator and a literal number operand. 
* The program counts different parts of the import statements as operands.
* In case of /// Should we add the operators in different scopes as two different things
* // Count integer lateral numbers only only	
* // Check if the parameters are set or not before calculating any metric
* // make the original parameters valid for division
since it is a reserved keyword from source level 5.0

* /// Test public, non static, protected.




## Authors

**Ahmed Metwally**