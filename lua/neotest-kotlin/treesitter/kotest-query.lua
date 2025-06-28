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

;; -- todo STRING SPEC --
;; -- todo BEHAVIOR SPEC --
;; -- todo FREE SPEC --
;; --- WORD SPEC ---

; Matches "context" `when` { /** body **/ }
; Matches "context" When { /** body **/ }

(infix_expression
  (string_literal) @namespace.name (#gsub! @namespace.name "$" " when")
  (simple_identifier) @function_name (#any-of? @function_name "`when`" "When")
  (lambda_literal)
) @namespace.definition

; Matches "context" should { /** body **/ }

(infix_expression
  (string_literal) @namespace.name (#gsub! @namespace.name "$" " should")
  (simple_identifier) @function_name (#eq? @function_name "should")
  (lambda_literal)
) @namespace.definition

; Matches "test" { /** body **/ }

(call_expression
  (string_literal) @test.name
    (call_suffix
      (annotated_lambda)
  )
) @test.definition

;; -- todo FEATURE SPEC --
;; -- todo EXPECT SPEC --
;; -- todo ANNOTATION SPEC --

]]
