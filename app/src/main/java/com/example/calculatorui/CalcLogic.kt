package com.example.calculatorui

import android.content.Context
import kotlin.math.pow

class CalcLogic {
private val indexStart = 0;

    fun boolSwap(bool: Boolean): Boolean = !bool

    //returns data in String from function loadMem
    fun mR(context: Context): String{
        return loadMem(context,"calcMem").toString()
    }
    //overwrites value saved at "calcMem" with "0" by passing to function "saveMem"
    fun mC(context: Context){
        saveMem(context,"0","calcMem")
    }

    /*
    adds value saved at "calcMem" with value passed when function is called
    saves result at "calcMem" by passing to saveMem function.
     */
    fun mPlus(context: Context,string: String){
        val mem = loadMem(context,"calcMem").toString().toDouble()+string.toDouble()
        saveMem(context,mem.toString(),"calcMem")
    }

    /*
    subtracts value saved at "calcMem" with value passed when function is called
    saves result at "calcMem" by passing to saveMem function.
    */
    fun mMinus(context: Context,string: String){
        val mem = loadMem(context,"calcMem").toString().toDouble()-string.toDouble()
        saveMem(context,mem.toString(),"calcMem")
    }

    /*
       saves to location "Memory" with key as "dataType" and value as "data"
       if location does not exist, location is created
    */
    fun saveMem(context: Context, data: String, dataType: String) {
        val sharedPreferences = context.getSharedPreferences("Memory", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString(dataType, data)
            apply()
        }
    }

    /*
      returns data from location "Memory" with key "dataType"
      if data does not exist, returns "null"
   */
    private fun loadMem(context: Context, dataType: String): String? {
        val sharedPreferences = context.getSharedPreferences("Memory", Context.MODE_PRIVATE)
        return sharedPreferences.getString(dataType, "null")
    }


    /*
    calculates the expression passed when function is called
    returns value of calculated expression
    */
    fun calculate(string: String): String {

        //passes string to "errorChecks" to check for format errors
        if (errorChecks(string)) {
            //converts string to MutableList
            var parts = stringToMutableList(string)

            //while the MutableList has more than one index, call expression logic in BEDMAS order
            while (parts.size > 1) {
                parts = pieFormat(parts)
                parts = percent(parts)
                parts = negPosCheck(parts)
                parts = exp(parts)
                parts = mulDiv(parts)
                parts = addSub(parts)
                println(parts)
            }
            //returns the result once finished
            return parts[0]
        }
        //returns Syntax Error if errorChecks returns false
        return "Syntax Error"
    }

    /*
    checks "string" for format errors when function is called
    returns false if error found
    returns true if no errors found
    */
    private fun errorChecks(string: String): Boolean {
        //if the string is empty, return false
        if (string.isEmpty()) return false
        //gets the first and last character of the string
        val firstString = string.first()
        val lastString = string.last()
        /* when the first character of the the string is a digit, negative sign, or pi character
        and
        last characters of the string are either a digit, pi, or %, character return true
        */
        val firstCheck = firstString.isDigit() || firstString  == 'Π' || firstString  == '-'
        val lastCheck = lastString.isDigit() || lastString  == 'Π' || lastString  == '%'


        return firstCheck && lastCheck
    }

     /*
     when function is called, checks and handles MutableList for positive and negative symbol logic
     returns MutableList with formatted positive negative logic
     */
    private fun negPosCheck(parts: MutableList<String>):MutableList<String> {
        var index = 0

         /*
         if the first character in the MutableList is "-",
         gets the second character and multiplies by -1 to convert it to a negative number
         than removes the "-" symbol at the first character
         */
        if(parts[0]=="-"){
            val nextPart = parts[1].toDouble()
            val result = nextPart * -1
            parts[1] = result.toString()
            parts.removeAt(index)
        }
         /*
         if the first character in the MutableList is "+",
        gets the second character and multiplies by 1 to convert it to a positive number
        than removes the "+" symbol at the first character
        */
        if(parts[0]=="+"){
            val nextPart = parts[1].toDouble()
            val result = nextPart * 1
            parts[1] = result.toString()
            parts.removeAt(index)
        }

         /*
         iterates through the MutableList
         converts double negatives to positives i.e. [1,-,-,1] -> [1,+,1,]
         handles inputs where a negative symbol occurs after another operator i.e. [1,*,-,3]
          */
        while (index < parts.size) {
/*
         when the "-" is detected, checks the previous character and next character
         if the next character is also a "-", converts the next character to a "+" sign than removes the initial "-"
 */
            when (parts[index]) {
                "-" -> {
                    val nextPart = parts[index + 1]
                    val prevPart = parts[index - 1]
                    if (index < parts.size - 1) {


                        if(nextPart == "-")  {
                            parts[index + 1] = "+"
                            parts.removeAt(index)
                            index--
                        }
                        /*
                           when the "-" is detected, checks the previous character
                           if character matches any non-digit, multiples the next character by -1 to convert it to a negative number
                           removes the detected "-" from the MutableList
                         */
                        else if(prevPart.matches(Regex("\\D+"))){
                            val result = nextPart.toDouble() * -1
                            parts[index + 1] = result.toString()
                            parts.removeAt(index)
                        }
                    }
                }
            }
            index++
        }
         //returns MutableList when finished
        return parts
    }

    /*
    converts string when function is called to MutableList of Strings by
    splitting the string on any decimal number, whole number, "Π" character, than any other single character
    returns the MutableList
     */
    private fun stringToMutableList(string: String): MutableList<String> {
        val regex = Regex("\\d+\\.\\d+|\\d+|Π|.")
        val matches = regex.findAll(string)
        return matches.map { it.value }.toMutableList()
    }

     fun formatCheck(string: String): String {
        val emptyString = ""
        if(string=="0")
            return emptyString
        return string
    }

    /*
    iterates through MutableList and looks for Pi character "Π"
    logic for handling cases where:
     - Π is detected after or before another digit
     - Π is detected after and before another digit
     - Π is detected between two operators
     returns MutableList
     */
    private fun pieFormat(parts: MutableList<String>):MutableList<String> {
        var index = indexStart

        while (index < parts.size) {
            when (parts[index]) {
                "Π" -> {

                    /*
                    gets the previous and next character in the MutableList
                    handles out of bound errors for next and previous character
                     */
                    val prevPart = if (index > 0) parts[index - 1] else ""
                    val nextPart = if (index + 1 < parts.size) parts[index + 1] else ""

                    /*
                    if the first character is any digit or decimal number
                    replace the "Π" symbol with "3.14159265359" and adds the * operator to the current index i.e. [3,Π] -> [3,*,3.14159265359]
                    else replace the "Π" symbol with "3.14159265359" i.e. Π -> 3.14159265359
                     */
                    if (index == 0 ) {
                        if (nextPart.matches(Regex("\\d+(\\.\\d+)?"))) {
                            parts[index] = "3.14159265359"
                            parts.add(index + 1, "*")
                        } else {
                            parts[index] = "3.14159265359"
                        }
                    }
                    /*
                    if the previous character and the next character are any digit or decimal number
                    replace the "Π" symbol with "3.14159265359", adds the * operator to the current index and the current index + 2 i.e. [3,Π,3] -> [3,*,Π,*,3]
                    if the previous character is a digit or decimal number, "Π" symbol with "3.14159265359", adds the * operator to the current index i.e. [3,Π] -> [3,*,Π]
                    if the next character is a digit or decimal number, "Π" symbol with "3.14159265359", adds the * operator to the next index i.e. [Π3] -> [Π,*,3]
                    if only the "Π" symbol is detected, replace with "3.14159265359"
                     */
                    else{
                        if(prevPart.matches(Regex("\\d+(\\.\\d+)?")) && nextPart.matches(Regex("\\d+(\\.\\d+)?"))) {
                            parts[index] = "3.14159265359"
                            parts.add(index, "*")
                            parts.add(index+2, "*")
                        }
                        else if (prevPart.matches(Regex("\\d+(\\.\\d+)?"))){
                            parts[index] = "3.14159265359"
                            parts.add(index, "*")
                        }
                        else if(nextPart.matches(Regex("\\d+(\\.\\d+)?"))){
                           parts[index] = "3.14159265359"
                            parts.add(index + 1, "*")
                        } else if(prevPart == "%"){
                            parts[index] = "3.14159265359"
                            parts.add(index, "*")
                        }
                        else {
                            parts[index] = "3.14159265359"
                        }
                    }
                }
            }
            index++
        }
        //returns MutableList when finished
        return parts
    }

    /*
    iterates through MutableList
    handles logic for percentages i.e.[5,%]
    returns MutableList
     */
    private fun percent(parts: MutableList<String>):MutableList<String>{
        var index = indexStart
        while (index < parts.size) {
            when (parts[index]) {
                "%" -> {
                    /*
                    when a "%" character is detected, divides the previous index value by 100
                    stores result in previous index
                    removes the current index
                    reduce index by one to account for index removal
                     */
                    if (index > 0){
                        val prevPart = parts[index - 1].toDouble()
                        val result = prevPart / 100
                        parts[index - 1] = result.toString()
                        parts.removeAt(index)
                        index--
                    }
                    /*
                    handles cases when '%' is followed directly by Π or a digit
                    adds a "*" symbol after "%" symbol
                   */
                    val nextPart = if (index + 1 < parts.size) parts[index + 1] else ""
                    if (nextPart.matches(Regex("\\d+(\\.\\d+)?"))) {
                        parts.add(index + 1, "*")
                    } else if (nextPart == "Π"){
                        parts.add(index + 1, "*")
                    }
                }
            }
            index++
        }
        //returns MutableList when finished
        return parts
    }

    /*
    iterates through MutableList when called
    handles logic for exponents
    returns MutableList
    */
    private fun exp(parts: MutableList<String>):MutableList<String>{
        var index = indexStart
        while (index < parts.size) {
            when (parts[index]) {
                "^" -> {
                    /*
                    when "^" is detected, raises the previous index to the power of the next index
                    stores the result in the previous index
                    removes the current index and next index from MutableList
                    reduces index by one to account for index removal
                    handles out of bounds exceptions
                     */
                    if (index > 0 && index < parts.size - 1) {
                        val prevPart = parts[index - 1].toDouble()
                        val nextPart = parts[index + 1].toDouble()
                        val result = prevPart.pow(nextPart)

                        parts[index - 1] = result.toString()
                        parts.removeAt(index)
                        parts.removeAt(index)
                        index--
                    }
                }
            }
            index++
        }
        //returns MutableList when finished
        return parts
    }

    /*
    iterates through MutableList when called
    handles logic for multiplication and Division i.e. [5,*,6] or [5,÷,6]
    returns MutableList
     */
    private fun mulDiv(parts: MutableList<String>):MutableList<String>{
        var index = indexStart
        while (index < parts.size) {
            when (parts[index]) {
                "*" -> {
                    /*
                   when "*" is detected, multiplies the previous index with the next index
                   stores the result in the previous index
                   removes the current index and next index from MutableList
                   reduces index by one to account for index removal
                   handles out of bounds exceptions
                   */
                    if (index > 0 && index < parts.size - 1) {
                        val prevPart = parts[index - 1].toDouble()
                        val nextPart = parts[index + 1].toDouble()
                        val result = prevPart * nextPart

                        parts[index - 1] = result.toString()
                        parts.removeAt(index)
                        parts.removeAt(index)
                        index--
                    }
                }
                "÷" -> {
                    /*
                    when "/" is detected, divides the previous index with the next index
                    stores the result in the previous index
                    removes the current index and next index from MutableList
                    reduces index by one to account for index removal
                    handles out of bounds exceptions
                    */
                    if (index > 0 && index < parts.size - 1) {
                        val prevPart = parts[index - 1].toDouble()
                        val nextPart = parts[index + 1].toDouble()
                        val result = prevPart / nextPart

                        parts[index - 1] = result.toString()
                        parts.removeAt(index)
                        parts.removeAt(index)
                        index--
                    }
                }
            }
            index++
        }
        //returns MutableList when finished
        return parts
    }

    /*
     iterates through MutableList when called
     handles logic for addition and subtraction i.e. [5,+,6] or [5,-,6]
     returns MutableList
      */
    private fun addSub(parts: MutableList<String>):MutableList<String>{
        var index = indexStart
        while (index < parts.size) {
            when (parts[index]) {
                "+" -> {
                    /*
                    when "+" is detected, adds the previous index with the next index
                    stores the result in the previous index
                    removes the current index and next index from MutableList
                    reduces index by one to account for index removal
                    handles out of bounds exceptions
                    */
                    if (index > 0 && index < parts.size - 1) {
                        val prevPart = parts[index - 1].toDouble()
                        val nextPart = parts[index + 1].toDouble()
                        val result = prevPart + nextPart

                        parts[index - 1] = result.toString()
                        parts.removeAt(index)
                        parts.removeAt(index)
                        index--
                    }
                }
                "-" -> {
                    /*
                    when "-" is detected, subtracts the previous index with the next index
                    stores the result in the previous index
                    removes the current index and next index from MutableList
                    reduces index by one to account for index removal
                    handles out of bounds exceptions
                    */
                    if (index > 0 && index < parts.size - 1) {
                        val prevPart = parts[index - 1].toDouble()
                        val nextPart = parts[index + 1].toDouble()
                        val result = prevPart - nextPart

                        parts[index - 1] = result.toString()
                        parts.removeAt(index)
                        parts.removeAt(index)
                        index--
                    }
                }
            }
            index++
        }
        //returns MutableList when finished
        return parts
    }

    //when called, returns true if last character a string is '-'
    fun lastNegCheck(string: String):Boolean {
        return string.last() == '-'
    }

    //when called, returns true if last character a string is '+'
    fun lastPosCheck(string: String):Boolean {
        return string.last() == '+'
    }

    /*
    when called, removes the last character a string if it is not empty
    returns the string when finished
    */
    fun backSpace(string: String): String{
        if(string.isNotEmpty()){
                return string.dropLast(1)
            }
        return string
        }
    //when called, returns true if last character a string is any non-number character
    fun lastCheck(string: String): Boolean{
        val strCheck = string.last().toString()
        return strCheck.matches(Regex("\\D+"))
    }
}