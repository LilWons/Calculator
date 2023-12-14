/*
    Author: LilWons

    Simple calculator that preform calculations on users inputted expressions.
    Calculator can save, read, add, and subtract from memory.
    Calculator can perform add, subtract, multiply, divide, percent, exponent, logic while following BEDMAS.
    Calculator has built in safe guards preventing formatting and calculation errors.
 */
package com.example.calculatorui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.calculatorui.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    //declaring variables
    private lateinit var binding: ActivityMainBinding
    private var opReady = false
    private var shift = false
    private val calcLogic = CalcLogic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //sets up memory with value of "0"
        calcLogic.saveMem(this@MainActivity,"0","calcMem")
        setListeners()
    }
    /*
    sets up listeners for buttons
    sets up logic for when setOnClickListeners are triggered
    */
    private fun setListeners(){
        with(binding){
            //calls numberLogic and passes "0"
            btZero.setOnClickListener{numberLogic("0")}
            //calls numberLogic and passes "1"
            btOne.setOnClickListener{numberLogic("1")}
            //calls numberLogic and passes "2"
            btTwo.setOnClickListener{numberLogic("2")}
            //calls numberLogic and passes "3"
            btThree.setOnClickListener{numberLogic("3")}
            //calls numberLogic and passes "4"
            btFour.setOnClickListener{numberLogic("4")}
            //calls numberLogic and passes "5"
            btFive.setOnClickListener{numberLogic("5")}
            //calls numberLogic and passes "6"
            btSix.setOnClickListener{numberLogic("6")}
            //calls numberLogic and passes "7"
            btSeven.setOnClickListener{numberLogic("7")}
            //calls numberLogic and passes "8"
            btEight.setOnClickListener{numberLogic("8")}
            //calls numberLogic and passes "9"
            btNine.setOnClickListener{numberLogic("9")}

            /*
            toggles shift from true to false and false to true
            calls colorUpdate to changes shift enabled button colour and text colour
             */
            btShift.setOnClickListener{shift = calcLogic.boolSwap(shift)
                colorUpdate()}

            //if opReady is true, call opLogic and pass "%"
            btPercent.setOnClickListener{if(opReady){numberLogic("%")}}
            //if opReady is true, call opLogic and pass "*"
            btTimes.setOnClickListener{if(opReady){opLogic("*")}}
            /*
            if tvExpression is empty, calls opLogic and pass "-"
            else if the last character is a "-", remove the last character and call opLogic and pass "+"
            else if the last character is "+", remove the last character and call opLogic and pass "-"
            else call opLogic and pass "-"
             */
            btMinus.setOnClickListener{
                if(tvExpression.text.toString()==""){
                    opLogic("-")
                }
                else if(calcLogic.lastNegCheck(tvExpression.text.toString())){
                    val buffer  = calcLogic.backSpace(tvExpression.text.toString())
                    val calcText = getString(R.string.calc,buffer)
                    tvExpression.text = calcText
                    opLogic("+")
                }
                else if(calcLogic.lastPosCheck(tvExpression.text.toString()))
                {
                    val buffer  = calcLogic.backSpace(tvExpression.text.toString())
                    val calcText = getString(R.string.calc,buffer)
                    tvExpression.text = calcText
                    if(opReady)opLogic("-")
                }
                else{opLogic("-")}
            }

            //if opReady is true, call opLogic and pass "+"
            btPlus.setOnClickListener{if(opReady){opLogic("+")}}
            //if opReady is true, call opLogic and pass "."
            btDecimal.setOnClickListener{if(opReady){opLogic(".")}}
            //removes the last character of the string than updates tvExpression's text with result
            btBackSpace.setOnClickListener{backSpace()}
            /*
            Sends the expression in tvExpression to be calculated
            Updates tvExpression with result
             */
            btEquals.setOnClickListener{
                // calls calculate from calcLogic and passes string in tvExpression.text
                    var buffer  = calcLogic.calculate(tvExpression.text.toString())
                // if the result is 0.0, convert to 0 for appearance purpose
                    if(buffer=="0.0"){
                        buffer="0"
                    }
                val calcText = getString(R.string.calc,buffer)
               //updates tvExpression.text with result from buffer
                tvCalculation.text = calcText
                tvExpression.text=""
            }

            /*
            if shift button has been pressed, clears the memory
            else writes "." to tvExpression
            */
            btDivide.setOnClickListener{
                if(shift){
                    // if shift is true, call mC from calcLogic and pass current context
                    calcLogic.mC(this@MainActivity)
            }
            else {
                //  if shift is not true and opReady is true, call opLogic and pass "."
                    if(opReady){opLogic("÷")}
            }
            }
            /*
            if shift button has been pressed, read from memory and display value on tvExpression
            else clear the value on tvExpression
            */
            btClear.setOnClickListener{
                if(shift){
                    // if shift is true, call mR from calcLogic and pass current context
                    var buffer  = calcLogic.mR(this@MainActivity)
                    //if the result is "0.0" convert to "0" for appearance
                    if(buffer=="0.0"){
                        buffer="0"
                    }
                    val calcText = getString(R.string.calc,buffer)
                    tvCalculation.text = calcText
                }
                else {
                    //call clear function
                    clear()
                }
            }
            /*
            if the shift button has been pressed, adds the current result to memory
            else writes exponent symbol to tvExpression
             */
            btExponent.setOnClickListener{
                if(shift){
                    //calls mPlus from calcLogic and passes context and tvExpression string
                    val buffer = tvCalculation.text.toString()
                    if(buffer.isNotEmpty())
                        calcLogic.mPlus(this@MainActivity,buffer)
            }
            else {
                //  if shift is not true and opReady is true, call opLogic and pass "^"
                if(opReady){opLogic("^")}
            }
            }
            /*
              if the shift button has been pressed, subtracts the current result from memory
              else writes Pi symbol to tvCalculation
            */
            btPie.setOnClickListener{
                if(shift){
                    //calls mMinus from calcLogic and passes context and tvExpression string
                    val buffer = tvCalculation.text.toString()
                    if(buffer.isNotEmpty())
                        calcLogic.mMinus(this@MainActivity,buffer)
                }
                else {
                    //  if shift is not true and opReady is true, call opLogic and pass "Π"
                    numberLogic("Π")
                    }
                }
        }
    }

    /*
    Updates the background and text color of the shift enabled buttons when shift has been pressed
    */
    private fun colorUpdate(){
        with(binding){
        if(shift){
            btPie.setBackgroundColor(ContextCompat.getColor(this@MainActivity,R.color.orange))
            btPie.setTextColor(Color.BLACK)
            btExponent.setBackgroundColor(ContextCompat.getColor(this@MainActivity,R.color.orange))
            btExponent.setTextColor(Color.BLACK)
            btClear.setBackgroundColor(ContextCompat.getColor(this@MainActivity,R.color.orange))
            btClear.setTextColor(Color.BLACK)
            btDivide.setBackgroundColor(ContextCompat.getColor(this@MainActivity,R.color.orange))
            btDivide.setTextColor(Color.BLACK)

        }else{
            btPie.setBackgroundColor(Color.BLACK)
            btPie.setTextColor(Color.WHITE)
            btExponent.setBackgroundColor(Color.BLACK)
            btExponent.setTextColor(Color.WHITE)
            btClear.setBackgroundColor(Color.BLACK)
            btClear.setTextColor(Color.WHITE)
            btDivide.setBackgroundColor(Color.BLACK)
            btDivide.setTextColor(Color.WHITE)
        }
        }
    }
    /*
     clears the text in tvExpression
     next input cannot be an operator other than "-"
     */
    private fun clear(){
        with(binding) {
            tvExpression.text = ""
            tvCalculation.text = ""
        }
        opReady = false
    }

    /*
    Checks the last character of the string, if it is a character, the next input can be an operator
    Removes the last character from the string in tvExpression
    Updates tvExpression with result
     */
    private fun backSpace(){
        with(binding) {
            if(calcLogic.lastCheck(tvExpression.text.toString())){
                opReady = true
            }
            val buffer = calcLogic.backSpace(tvExpression.text.toString())
            val calcText = getString(R.string.calc,buffer)
            tvExpression.text = calcText
            }
        }

    /*
    when called, joins passed string "num" with text from tvExpression
    tvExpression is than updated with the result from buffer
    allows operator to be the next input
     */
    private fun numberLogic(num: String){
        with(binding) {

            val buffer = calcLogic.formatCheck(tvExpression.text.toString()) + num
            val calcText = getString(R.string.calc,buffer)
            tvExpression.text = calcText
            opReady = true
        }
    }

    /*
     if operator is allowed as input,
     when called, joins passed string "op" with text from tvExpression
     tvExpression is than updated with the result from buffer
     the next input cannot be an operator
     */
    private fun opLogic(op: String){
        with(binding) {
            val buffer = tvExpression.text.toString() + op
            val calcText = getString(R.string.calc, buffer)
            tvExpression.text = calcText
            opReady = false
        }
    }
}