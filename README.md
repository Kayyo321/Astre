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

## Printing

There is an `io` library in Astre already, but you can use the built-in print system just as well (although it doesn't support printing to the Std-Err stream).

```
print "Hello, World!";
```

You can only print one value at a time, so to print on the same line, use the `!` operator after print to specify that you don't want to print a new-line.

```
print! "Hi";
print "There";
```

of course if you want to use the bang operator like it's supposed to be used, wrap it in parenthesis:

for example:

```
let my_boolean = true;

print! (!my_boolean); // false
```

## Functions

In Astre, functions are defined using the `function`, `func`, or `fn` keywords (each work the same way).

for example:

```
function add_nums(x, y) {
   return x + y;
}
```

functions are called using C-syntax.

for example:

```
function add_nums(x, y) {
   return x + y;
}

let twenty_two = add_nums(20, 2);
```

functions are defined as callable-values; meaning you can store a functor (function pointer) in a variable!

for example:

```
function greet(person) {
   print "Nice to meet you " + person + "!";
}

let say_hi = greet;

say_hi('Wahid'); 

// prints: `Nice to meet you Wahid!`
```

to make a function have a static lifetime (or be available for use earlier in the program), declare it like so:

```
function foo() (static) {
   print "bar";
}
```

## Structs

Structures in Astre are basically classes, but fields are defined dynamically, (and we love inheritance!).
You can define them using the `struct`, or `class` keyword (both work the same).

for example:

``` 
struct Human {
}

let sully = Human();

sully.name = "Sully";
sully.age = 14;
```

You can add methods to structs like so:

```
struct Bagel {
   topping() {
      print "Cream Cheese!";
   }  
}

let my_bagel = Bagel();
my_bagel.topping();
```

You can create a constructor by adding a `anew` method to your struct.

for example:

```
struct Breakfast {
   anew(meat, bread) {
      self.meat = meat;
      self.bread = bread;
   }
}

let healthy_breakfast = Breakfast('bacon', 'sourdough');
```

The `self` keyword refers to the current struct-instance.

for example:

```
struct Turtle {
   anew(name) {
      self.name = name;
   }
   
   get_name() {
      return self.name;
   }
}

let sally = Turtle("Sally");
print sally.get_name(); 

// prints: `Sally`
```

You can inherit a structures fields and properties by using the `derives` keyword.

for example

```
struct Donut {
   how_to_prepare() {
      print "Fry until golden brown";
   }
}

struct BostonCream derives Donut {
   how_to_prepare() {
      super.how_to_prepare();
      print "Pipe with custard and slather with chocolate!";
   }
}

let boston_cream_donut = BostonCream().how_to_prepare();

// prints: `Fry until golden brown`
//         `Pipe with custard and slather with chocolate!`
```

To make a struct have a static lifetime (or be available earlier in the program) declare it like so:

```
struct Foo (static) {
   bar() {
      print "buz";
   }
}
```

To make a method in a struct static, declare it like this instead:

```
struct Foo {
   bar() (static) {
      print "buz";
   }
}

Foo.bar(); // call bar without an instance of Foo
```

> Both struct and member can be static at once!

## Interfaces

An interface is a sort of guidline for structures to follow. If a struct is declared to impliment an interface, is **must** follow it's rules exactly.

An interface is defined as so

```
interface Math {
   subtract(2);
   add_3_nums(3);
   square_root(1);
}
```

each method in the interface has to give the amount of arguments it requires, 

for example

```
interface Test {
   take_no_args();
   also_take_none(0);
   take_13_args(13);
}
```

to apply an interface to a structure, use the `implements` keyword.

for example

```
interface Pet {
   sound();
}

struct Dog implements Pet {
   sound() {
      print "GRRRR!";
   }
}

struct Cat implements Pet {
   sound() {
      print "MEOWMEOW!";
   }
}
```

to make the interface have a static lifetime (or be available for use earlier in the program), declare it as static like so:

```
interface Pet (static) {
   sound();
}
```
## Match

To compare between lots of values, turn to the `match` operation. Similar to `switch` in other languages, you take an input value and test it against multiple cases.

for example

```
import('random');

let! rand_num = randint(1,3);

match (rand_num) {
   case 1 {
      print "Un!";
   }
   case 2 {
      print "Deux!";
   }
   case 3 {
      print "Trois!";
   }
}
```

to handle a value that wasn't covered by the case's, use the `else` keyword.

for example

```
import('io');

let! user_num = read_num('Enter a number 1-3: ');

match (user_num) {
   case 1 {
      print "Un!";
   }
   case 2 {
      print "Deux!";
   }
   case 3 {
      print "Trois!";
   }
} else {
   print! user_num;
   print " Isn't between 1 & 3!!";
}
```

when Astre encounters a match statement, it evaluates each case and check's if it matches one-by-one. This may not be efficient
if the values are never going to change. Use can make a `match` statement static (meaning it generates it's values at compile time) like so:

```
match (user_num) (static) {
   case 1 {
      print "Un!";
   }
   case 2 {
      print "Deux!";
   }
   case 3 {
      print "Trois!";
   }
}
```

now it will no longer loop through each case, and instead jump to the correct one.

## Loops

### For

The for loop in Astre is exactly as it is in javascript, C, and everything under the sun!

```
for (let i = 0; i < 10; i = i + 1) {
   print i;
}
```

### While

The while loop in Astre is also exactly how it sounds.

```
let i = 0;
while (i < 10) {
   print i;
   i = i + 1;
}
```

### Range

Range statements are made to mirror something like `python`'s for-loop iteration, they are defined as so:

```
range (i : 10) {
   print i;
}
```

of course `python`'s range function has an overload that allows you to change the `start`, `stop`, and `step`.
To do this, mirror the example below:

```
range (j : 0, 20, 5) {
   print j;
}
```
