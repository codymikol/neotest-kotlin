-- These are treesitter queries for pulling data out of the AST,
-- More information on how this works over here: https://neovim.io/doc/user/treesitter.html
-- And you can interactively play around with these kotlin queries here: https://fwcd.github.io/tree-sitter-kotlin/
return [[

;; --- DESCRIBE SPEC ---

; Matches namespace describe("context") { /** body **/ }

(call_expression 
  (simple_identifier) @function_name (#eq? @function_name "describe")
    (call_suffix 
      (value_arguments 
        (value_argument 
          (string_literal) @namespace.name
        )
      ) (annotated_lambda)
    )
) @namespace.definition

; Matches test it("context") { /** body **/ }

(call_expression 
  (simple_identifier) @function_name (#eq? @function_name "it")
    (call_suffix 
      (value_arguments 
        (value_argument 
          (string_literal) @test.name 
        )
      ) (annotated_lambda)
    ) 
) @test.definition

; todo Matches xdescribe("context") { /** body **/ }

; todo Matches xit("context") { /** body **/ }

;; --- FUN SPEC ---

; Matches namespace context("context") { /** body **/ }

(call_expression 
  (simple_identifier) @function_name (#eq? @function_name "context")
    (call_suffix 
      (value_arguments 
        (value_argument 
          (string_literal) @namespace.name
        )
      ) (annotated_lambda)
    )
) @namespace.definition

; Matches test test("context") { /** body **/ }

(call_expression 
  (simple_identifier) @function_name (#eq? @function_name "test")
    (call_suffix 
      (value_arguments 
        (value_argument 
          (string_literal) @test.name 
        )
      ) (annotated_lambda)
    ) 
) @test.definition

;; --- SHOULD SPEC ---

; Matches test should("context") { /** body **/ }

(call_expression 
  (simple_identifier) @function_name (#eq? @function_name "should")
    (call_suffix 
      (value_arguments 
        (value_argument 
          (string_literal) @test.name 
        )
      ) (annotated_lambda)
    ) 
) @test.definition

;; --- STRING SPEC ---

; Matches "test" { /** body **/ }

(call_expression
  (string_literal) @test.name
    (call_suffix
      (annotated_lambda)
  )
) @test.definition

;; -- todo BEHAVIOR SPEC --
;; --- FREE SPEC ---

; Matches "context" - { /** body **/ }

(additive_expression
  (string_literal) @namespace.name
  (lambda_literal)
) @namespace.definition

; Matches "test" { /** body **/ }

(call_expression
  (string_literal) @test.name
    (call_suffix
      (annotated_lambda)
  )
) @test.definition

;; -- todo WORD SPEC --
;; -- todo FEATURE SPEC --
;; --- FEATURE SPEC ---

; Matches namespace feature("context") { /** body **/ }

(call_expression 
  (simple_identifier) @function_name (#eq? @function_name "feature")
    (call_suffix 
      (value_arguments 
        (value_argument 
          (string_literal) @namespace.name
        )
      ) (annotated_lambda)
    )
) @namespace.definition

; Matches test scenario("context") { /** body **/ }

(call_expression 
  (simple_identifier) @function_name (#eq? @function_name "scenario")
    (call_suffix 
      (value_arguments 
        (value_argument 
          (string_literal) @test.name 
        )
      ) (annotated_lambda)
    ) 
) @test.definition

;; --- EXPECT SPEC ---

; Matches test expect("context") { /** body **/ }

(call_expression 
  (simple_identifier) @function_name (#eq? @function_name "expect")
    (call_suffix 
      (value_arguments 
        (value_argument 
          (string_literal) @test.name 
        )
      ) (annotated_lambda)
    ) 
) @test.definition

;; -- todo ANNOTATION SPEC --

]]
