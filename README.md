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

## Functions

In Astre, functions are defined using the `function` keyword.

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

## Structs

Structures in Astre are basically classes, but fields are defined dynamically, (and we love inheritance!).

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