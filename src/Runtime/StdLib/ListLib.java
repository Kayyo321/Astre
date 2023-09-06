package Runtime.StdLib;

import java.util.List;
import java.util.function.Consumer;

import LexicalAnalysis.Scanner;
import LexicalAnalysis.Token;
import Parsing.Parser;
import Parsing.Stmt;
import Runtime.Interpreter;

public class ListLib {
    public final static Consumer<Interpreter> builder = ListLib::build;

    private static void build(final Interpreter environment) {
        //region code-literal
        final String listStructToParse = """
                struct List {
                    anew() {
                        self.next = nothing;
                        self.value = nothing;
                    }

                    len() {
                        let this = self;
                        let? next = self.next;
                        let length = 0;

                        while (next != nothing) {
                            this = next;
                            next = this.next;
                            length = length+1;
                        }

                        return length;
                    }

                    push(value) {
                        let this = self;
                        let? next = self.next;

                        while (next != nothing) {
                            this = next;
                            next = this.next;
                        }

                        this.value = value;
                        this.next = List();
                    }

                    push_front(value) {
                        if (self.is_empty()) {
                            self.value = value;
                            return;
                        }

                        let cpy = self.make_copy();
                        self.clear();
                        self.push(value);
                        self.append(cpy);
                    }

                    pop() {
                        self.put(self.len()-1, nothing);
                    }

                    pop_front() {
                        if (self.is_empty()) {
                            return;
                        }

                        if (self.len() == 1) {
                            self.clear();
                            return;
                        }

                        let cpy = self.make_copy();
                        cpy = cpy.next;
                        self.clear();
                        self.append(cpy);
                    }

                    get(index) {
                        if (index < 0) {
                            return nothing;
                        }

                        let this = self;
                        let? next = self.next;
                        let idx = 0;

                        while (next != nothing) {
                            if (idx == index) {
                                return this.value;
                            }

                            this = next;
                            next = this.next;
                            idx = idx+1;
                        }

                        return this.value;
                    }

                    put(index, value) {
                        if (index < 0) {
                            return value;
                        }

                        let this = self;
                        let? next = self.next;
                        let idx = 0;

                        while (next != nothing) {
                            if (idx == index) {
                                this.value = value;
                                return value;
                            }

                            this = next;
                            next = this.next;
                            idx = idx+1;
                        }

                        return value;
                    }

                    is_empty() {
                        return self.value == nothing;
                    }

                    clear() {
                        self.value = nothing;
                        self.next = nothing;
                    }

                    front() {
                        return self.value;
                    }

                    back() {
                        return self.get(self.len()-1);
                    }

                    append(appending_list) {
                        let? ptr = appending_list;
                        while (ptr != nothing) {
                            if (ptr.value != nothing) {
                                self.push(ptr.value);
                            }
                            ptr = ptr.next;
                        }
                    }

                    print_list() {
                        let? ptr = self;
                        while (ptr != nothing) {
                            if (ptr.value != nothing) {
                                print ptr.value;
                            }

                            ptr = ptr.next;
                        }
                    }

                    make_copy() {
                        let new_list = List();
                        new_list.append(self);
                        return new_list;
                    }

                    insert(index, value) {
                        if (index < 0) {
                            return value;
                        }

                        let this = self;
                        let? next = self.next;
                        let idx = 0;

                        while (next != nothing) {
                            if (idx == index) {
                                let cpy = next.make_copy();
                                next.clear();
                                next.push(value);
                                next.append(cpy);
                                return;
                            }

                            this = next;
                            next = this.next;
                            idx = idx+1;
                        }

                        self.push(value);
                    }

                    swap(other_list) {
                        let this_cpy = self.make_copy();
                        let other_cpy = other_list.make_copy();

                        self.clear();
                        self.append(other_cpy);

                        other_list.clear();
                        other_list.append(this_cpy);
                    }

                    reverse() {
                        let cpy = self.make_copy();
                        let new = List();

                        for (let i = self.len(); i >= 0; i = i - 1) {
                            new.push(self.get(i));
                        }

                        self.clear();
                        self.append(new);
                    }
                    
                    fill(amount, value) {
                        self.clear();
                        
                        self.value = value;
                        self.next = List();
                        let? ptr = self.next;
                        
                        // go one below the amount because self.value counts as 1
                        for (let i = 1; i < amount; i = i + 1) {
                            ptr.value = value;
                            ptr.next = List();
                            ptr = ptr.next;
                        }
                    }
                }""";
        //endregion

        final Scanner lexer = new Scanner(listStructToParse);
        final List<Token> tokens = lexer.scan();
        final Parser parser = new Parser(tokens);
        final List<Stmt> parsed = parser.parse();

        environment.interpret(parsed);
    }
}
