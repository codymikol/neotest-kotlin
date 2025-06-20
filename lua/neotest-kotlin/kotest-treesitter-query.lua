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

; todo Mathes xdescribe("context") { /** body **/ }

; todo Mathes xit("context") { /** body **/ }

;; -- todo FUN SPEC --
;; -- todo SHOULD SPEC --
;; -- todo STRING SPEC --
;; -- todo BEHAVIOR SPEC --
;; -- todo FREE SPEC --
;; -- todo WORD SPEC --
;; -- todo FEATURE SPEC --
;; -- todo EXPECT SPEC --
;; -- todo ANNOTATION SPEC --

]]
