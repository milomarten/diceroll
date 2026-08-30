# Dice Roll
Extrapolation and improvement over [Fracktail](https://github.com/milomarten/fracktail)'s dice-rolling capabilities.

The ultimate goal of this library is to provide an easily-extensible dice rolling simulation, supporting syntax similar
to other dice-rolling libraries (such as Roll20's). This library also seeks to reduce as many restrictions as possible
as to the expressions you can construct, within certain safe limits. 

## Values
The library breaks the problem down into Terms, which represent a value, and Operations, which combine Terms into
new Terms. All types of terms and their operations will be explained here:

### Number
A number is any rational number. `+`, `-`, and `.` are supported, but not `e`.

Operations:
- Add, Subtract, Multiply, Divide, Root: the right-hand term is converted to a Number, and the result is a Number.
- Dice: 
  - If the right-hand term is a pool, a dice is constructed with one face per value in the pool (ignoring dropped values),
    and it is rolled `this` many times. The result is a DieResult
  - Otherwise, the right-hand term is converted to a Number. A dice is constructed with faces between 1 and that number
    (inclusive), and it is rolled `this` many times. The result is a DieResult. A negative left- or right-side will fail.

A Number can be resolved to a String.

### String
A string is any combination of characters. Strings must be enclosed in either single-quotes or double-quotes. A
backslash can be used as an escape character.

Operations:
- Add: The right-hand term is converted to a String, and the result is a String equal to both strings concatenated.
- Multiply: The right-hand term is converted to a Number, and the result is a String equal to the original string repeated
  that many times.

### Pool
This is a term which represents a collection of other terms; consider it like an array. The contents do not need
to be of the same type.

A Pool can be resolved into a Number only in the following circumstances:
- The pool contains exactly one non-dropped term, which may be resolved into a Number
- The pool has success or failure counting mode on

A pool can be resolved into a String only if the pool contains exactly one non-dropped term which may be resolved into
a String.

Operations:
- Add, Subtract, Multiply, Divide, Root: the right-hand side is resolved into a Number and the operation occurs
  on each element in the pool. The result is a Pool.
- Drop Highest/Lowest: The right-hand term is resolved into a Number. The highest/lowest elements are dropped. The result is a Pool
- Keep Highest/Lowest: The right-hand term is resolved into a Number. All except the highest/lowest elements are dropped. The result is a Pool
- Count Success/Count Failure: The Pool is put into counting mode. When converting this Pool into a Number, the
  result is the number of elements matching the `success` predicate, minus the number of elements matching the `failure` predicate (if any).
  The result is a Pool.

### DieResult
Represents a collection of dice rolls. This is similar to a Pool, however:
1. DieResults by default are initiated with a strategy for totaling up the results into one value, if applicable
2. DieResults supports rerolling operations, to grow the pool in a dynamic way

A DieResult can be resolved into a Number only in the following circumstances:
- In normal (summing) mode, all results are Numbers.
- The DieResult has success or failure counting mode on.

A DieResult can be resolved into a String only if the DieResult contains exactly one non-dropped term which may be
resolved into a String.

Operations:
- Add, Subtract, Multiply, Divide, Root: `this` and the right-hand side are resolved into Numbers and the operation occurs. 
  The result is a Number.
- Drop Highest/Lowest: The right-hand term is resolved into a Number. The highest/lowest elements are dropped. The result is a DieResult
- Keep Highest/Lowest: The right-hand term is resolved into a Number. All except the highest/lowest elements are dropped. The result is a DieResult
- Count Success/Count Failure: The DieResult is put into counting mode. When converting this DieResult into a Number, the
  result is the number of elements matching the `success` predicate, minus the number of elements matching the `failure` predicate (if any).
  The result is a DieResult.
- Explode: Any rolls passing some predicate result in an additional roll being made, stacking infinitely.
- Reroll: Any rolls passing some predicate result are dropped, and an additional roll is made, stacking infinitely.

## Tokens
The library has support for tokens, which can act as read-only variables. The calling program can provide a resolver
function to the EvaluatorOptions which handles the logic for resolving the token name into a true value. Sample use
cases are predefined tables, or automatically pulling values from another configuration such as character sheets.

All tokens start with an at sign, and may be any combination of letters (any case) and underscores.

## Operations
Operations are what's used to combine multiple terms into one term in some way. Operations are typically binary in
nature, but some are postfix (modify the term they are attached to without further input).

The behavior of each operation is dictated by the left-most term (in binary operations), or the postfixed term (in
postfix operations). As such, more information on each operation can be found in the term descriptions above.

### Parenthesis and Brackets
All Operations have a priority, to determine what order they should occur when chained together. Like the
PEMDAS or BEDMAS you learned in school: Addition and Subtraction are the lowest priority, followed by multiplication and
division, and root above that. For the bot's purposes, all Pool and DieResult operations are above root in priority.

Parenthesis () and Brackets [] are both supported, and are at the highest priority always.

As an example: `3 + 4 * 2` would yield 11 (multiplication first, then addition), but `(3 + 4) * 2` would yield 14 (addition
first, then multiplication).

### Addition, Subtraction, Multiplication, Division, Root
All of these operations are written in the same format as learned in school: `a + b`, `a - b`, `a * b`, `a / b`, and
`a√b`.

### Dice
To roll 1 or more dice, the format is `a d b`. `a` may be omitted, where it is assumed to be 1.

### Keep/Drop Highest/Lowest
The format is `a kh b`, `a kl b`, `a dh b`, and `a dl b`. Unlike some other diceroll libraries, `b` is always
required. All of these operations will fail if the DiceResult or Pool don't have a concept of "highest" and "lowest"
(i.e. all elements MUST be able to be coerced into a Number).

### Explode, Reroll, Target Success, Target Failure
These formats are all in the form of `a OPERATORSYMBOL b`. 
`OPERATOR` is the actual operation that needs to be done. Options are:
- `!` for explode
- `r` for reroll
- `s` for target success
- `f` for target failure
`SYMBOL` represents the threshold for which elements to affect with the operation. Options are:
- `=` for equals/included in
- `<` for less than or equals
- `>` for greater than or equals

Note that `OPERATOR` and `SYMBOL` must NOT have a space between them.

For Explode (`!`) only, `SYMBOL b` is optional. The predicate will instead become equal to the highest possible roll on
the dice

### Curly braces and Commas
Pools can be created by encapsulating terms in curly braces {} and separated with commas. The curly braces are mostly
syntactic sugar, and are only necessary for creating a pool of 1 term.

Example: `{a, b, c, ...}`