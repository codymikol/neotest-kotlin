-- These are treesitter queries for pulling data out of the AST,
-- More information on how this works over here: https://neovim.io/doc/user/treesitter.html
-- And you can interactively play around with these kotlin queries here: https://fwcd.github.io/tree-sitter-kotlin/

return [[

;; --- DESCRIBE SPEC ---

; Matches namespace describe("context") { /** body **/ }

(call_expression
	(call_expression
	  (simple_identifier) @func_name (#eq? @func_name "describe")
      (call_suffix
        (value_arguments
          (value_argument
            (string_literal) @namespace.name
          )
        )
      )
    )
) @namespace.definition

; Matches test it("context") { /** body **/ }

(call_expression
	(call_expression
	  (simple_identifier) @func_name (#eq? @func_name "it")
      (call_suffix
        (value_arguments
          (value_argument
            (string_literal) @test.name
          )
        )
      )
    )
) @test.definition

; Mathes xdescribe("context") { /** body **/ }

(call_expression
	(call_expression
	  (simple_identifier) @func_name (#eq? @func_name "xdescribe")
      (call_suffix
        (value_arguments
          (value_argument
            (string_literal) @namespace.name
          )
        )
      )
    )
) @namespace.definition

; Mathes xit("context") { /** body **/ }

(call_expression
	(call_expression
	  (simple_identifier) @func_name (#eq? @func_name "xit")
      (call_suffix
        (value_arguments
          (value_argument
            (string_literal) @test.name
          )
        )
      )
    )
) @test.definition

;; -- FUN SPEC --
; Matches test("context") { /** body **/ }
(call_expression
	(call_expression
	  (simple_identifier) @func_name (#eq? @func_name "test")
      (call_suffix
        (value_arguments
          (value_argument
            (string_literal) @test.name
          )
        )
      )
    )
  ) @test.definition

; Matches xtest("context") { /** body **/ }
(call_expression
	(call_expression
	  (simple_identifier) @func_name (#eq? @func_name "xtest")
      (call_suffix
        (value_arguments
          (value_argument
            (string_literal) @test.name
          )
        )
      )
    )
  ) @test.definition

; Matches namespace xcontext("context") { /** body **/ }

(call_expression
  (call_expression
    (simple_identifier) @func_name (#eq? @func_name "xcontext")
      (call_suffix
        (value_arguments
          (value_argument
            (string_literal) @namespace.name
          )
        )
      )
    )
  ) @namespace.definition

; Matches namespace context("context") { /** body **/ }

(call_expression
	(call_expression
	  (simple_identifier) @func_name (#eq? @func_name "context")
      (call_suffix
        (value_arguments
          (value_argument
            (string_literal) @namespace.name
          )
        )
      )
    )
) @namespace.definition

;; -- todo SHOULD SPEC --
;; -- todo STRING SPEC --
;; -- todo BEHAVIOR SPEC --
;; -- todo FREE SPEC --
;; -- todo WORD SPEC --
;; -- todo FEATURE SPEC --
;; -- todo EXPECT SPEC --
;; -- todo ANNOTATION SPEC --

]]
