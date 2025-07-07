-- These are treesitter queries for pulling data out of the AST,
--
-- See CONTRIBUTING.md
return [[

;; --- DESCRIBE SPEC ---

; Matches namespace describe("context") { /** body **/ }

(call_expression 
  (simple_identifier) @function_name (#eq? @function_name "describe")
    (call_suffix 
      (value_arguments 
        (value_argument 
          (string_literal
            (string_content) @namespace.name
          ) 
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
          (string_literal
            (string_content) @test.name
          )
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
          (string_literal
            (string_content) @namespace.name
          )
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
          (string_literal
            (string_content) @test.name
          ) 
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
          (string_literal
            (string_content) @test.name
          )
        )
      ) (annotated_lambda)
    ) 
) @test.definition

;; --- STRING SPEC ---

; Matches "test" { /** body **/ }

(call_expression
  (string_literal
    (string_content) @test.name
  ) 
  (call_suffix
    (annotated_lambda)
  )
) @test.definition

;; -- todo BEHAVIOR SPEC --
;; --- FREE SPEC ---

; Matches "context" - { /** body **/ }

(additive_expression
  (string_literal
    (string_content) @namespace.name
  )
  (lambda_literal)
) @namespace.definition

; Matches "test" { /** body **/ }

(call_expression
  (string_literal 
    (string_content) @test.name
  )
  (call_suffix
    (annotated_lambda)
  )
) @test.definition

;; --- WORD SPEC ---

; Matches "context" `when` { /** body **/ }
; Matches "context" When { /** body **/ }

(infix_expression
  (string_literal
    (string_content) @namespace.name (#gsub! @namespace.name "$" " when")
  )
  (simple_identifier) @function_name (#any-of? @function_name "`when`" "When")
  (lambda_literal)
) @namespace.definition

; Matches "context" should { /** body **/ }

(infix_expression
  (string_literal
    (string_content) @namespace.name (#gsub! @namespace.name "$" " should")
  )
  (simple_identifier) @function_name (#eq? @function_name "should")
  (lambda_literal)
) @namespace.definition

; Matches "test" { /** body **/ }

(call_expression
  (string_literal
    (string_content) @test.name
  )
    (call_suffix
      (annotated_lambda)
  )
) @test.definition

;; --- FEATURE SPEC ---

; Matches namespace feature("context") { /** body **/ }

(call_expression 
  (simple_identifier) @function_name (#eq? @function_name "feature")
    (call_suffix 
      (value_arguments 
        (value_argument 
          (string_literal
            (string_content) @namespace.name
          ) 
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
          (string_literal
            (string_content) @test.name
          )
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
          (string_literal
            (string_content) @test.name
          )
        )
      ) (annotated_lambda)
    ) 
) @test.definition

;; --- ANNOTATION SPEC ---

; Matches @Test fun Test() { /** body **/ }
; Doesn't Match @Ignore annotated functions

(function_declaration
  (modifiers
    (annotation
      (user_type
        (type_identifier) @annotation_name
      )
    )+ @annotations (#vim-match? @annotations "\%(\Ignore\)\@<!Test")
  )
  (simple_identifier) @test.name
  (function_value_parameters)
  (function_body)
) @test.definition

]]
