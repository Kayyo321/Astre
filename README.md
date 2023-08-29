# Astre
Astre ('Celestial body' in French) is an interpreted programming language designed by AFSE [Academy For Software Engineering (High School)] students: Sullivan Baczynski-Bruce, and Wahid Hussain. Their contributions are described more in-depth later... Happy-Coding!

# Theory

This project has taken many values from Sully's experience and have implements them into the interpreter.
Most importantly:

1. Readability: Functions should be kept to as little lines as possible to prevent confusion
2. Strict Initialization: This language's interpreter only initializes values *outside* of loops, meaning that
   the rate at which new memory is initialized & deleted is kept to a minimum.

# Workloads

This project is developed & maintained by Sullivan Baczynski-Bruce & Wahid Hussain, the following workloads show what has (and what needs to be) done by whom.

- [x] Lexical Analysis (Sully)
- [x] Parsing (Sully)
- [x] Runtime (Sully)
- [ ] Standard Library (Wahid)
      
# How-To

## Variables

In Astre, variables are defined using the `let` keyword. 

for example:

```let x = 1 + 2 * 3;``` (7)

to declare a variable as constant, use `!` after `let`.

for example:

```let! my_name = 'Sullivan';```

to allow a variable to contain `nothing` (otherwise known as `nil`, or `null`), add `?` after `let`.

for example:

```
let? my_age = nothing;
print my_age; // nothing
my_age = 14;
print my_age; // 14
```

you can chain these two operators.

for example:

```let!? my_variable = nothing; // constant 'nothing' value```