# EverScript

EverScript is a statically-typed programming language based on Bob Nystrom's Lox from CraftingInterpreters. 

The language was already fine as-is to write small programs, but I decided to add even more
features to allow users to create more complex programs.

**Attention** These docs are not finished. They will be improved as EverScript's development progresses.

<br>

# Goals

Currently, there are no specific goals. Maybe a JavaScript compiler that will I will start working on soon and will be added to the repository but don't expect much during the early stages.

<br>

# Some of the design choices I made for EverScript

First off, the syntax ISN'T final and is subjected to change at any given time. For the syntax design, I based it off of JavaScript and maybe some Python even though that's barely noticeable.

<br>

# Features that have been implemented 

- Arrays:
  - Arrays work like in any other language;
  - The elements in an array are expressions.

- Object literals:
  - Like arrays, they work like in any other language;
  - Object literal keys can only be strings, numbers or identifiers;
  - Object literal keys can also be variables. The value of the key will be the value of the variable;
  - Object literal values are expressions;
  - You can set objects in object literals, even if the key is not present in the object.

- Static methods:
  - Static methods cannot access `this.` or `super.`;
  - Static methods can be inherited by the parent class;

- Lambdas;

- Imports with namespaces:
  - You can import non-native libraries of EverScript with imports and assign them a namespace.

- Enums:
  - Enums in EverScript take a `n` number of values/properties/elements;
  - The value of an enum value/property/element is the name of the value in a string interpretation.

- Optional chaining/safe navigation operator:
  - Safely navigate through objects with the `?.` operator;
  - If the property does not exist, it will return null;
  - Only works with objects for the time being, but will be expanded to enums, classes and others.

- `throw` keyword:
  - Handle errors in EverScript with the `throw` statement;
  - You can only throw errors if they are classes and inherit the native `Error` class;
    - You must implement the `message() => {}` method in the class;
  - EverScript already comes with two native classes to handle errors:
    - `UnhandledException(message: string)`;
    - `Exception(type: string, message: string)`;
  
- Class setters and getters.

If you find any features that haven't been listed, please open an issue ticket or pull request. I might have forgotten something.

<br>

# Some examples of the basic "Hello, world!" program written in EverScript

```js
print "Hello, world!";
```

```js
(fn() => {
  print "Hello, world!";
})();
```

```js
let program = fn() => {
  print "Hello, world!";
};

program();
```

```js
fn program() => {
  print "Hello, world!";
}

program();
```

```js
let hello = { world: "Hello, world!" };
print hello.world;
```

```js
let array = [ "Hello, ", "world!" ];
print array[0] + array[1];
```

```js
class Hello: {
  static world() => {
    print "Hello, world!";
  }
}

Hello.world();
```

<br>

There are definitely some more creative ways, but these should be enough to demonstrate the current syntax. Semicolons ARE explicit at the moment. Wherever you see semicolons in the examples, you should use them in your code too.