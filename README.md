# Introduction

CDAP provides extensive support for user defined directives (UDDs) as a way to specify custom processing for DataPrep in CDAP. CDAP UDDs can currently be implemented in Java.

This UDD uses a Ceasar cipher to scramble text.  It is not meant to be used in cases where you need strong encryption as Ceasar cihper is not very secure and any text scrambled with it can be easily unscrambled.

With this UDD you will be able to scramble columns of data so that the masked field can be used in join operations. 


## Usage Example

mask-scramble :<column_name> <cipher> <shift>

mask-scramble :firstName ceasar 9

mask-scramble :lastName rot13