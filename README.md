# Calculate Halstead Metrics

This project is intended to calculate the Halstead complexity metrics (https://en.wikipedia.org/wiki/Halstead_complexity_measures)



## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisities

What things you need to install the software and how to install them

```
Give examples
```

### Installing


Clone the project to your local repository:
```
git clone https://ametwally@bitbucket.org/ametwally/hw1_ahmedmetwally.git
```


Navigate to the project's main directory. Then build the project using gradle 
```
gradle build
```


To execute the program:
```
gradle execute
```



Example: 
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


## Authors

* **Ahmed Metwally** - *Initial work*




**TODO:**

* // print distinct number of operator and operand 
* /// Test public, non static, protected.
* /// LambdaExpression is added in JLS8 API
* /// catch enhanced for and if condition for the : 
* // catch switch for the :
* /// negative numbers counted as 
* /// the program count import statements as Operands
* /// Should we add the operators in different scopes as two different things
* // Count integer lateral numbers only only	
* // Check if the parameters are set or not before calculating any metric
* // make the original parameters valid for division


since it is a reserved keyword from source level 5.0