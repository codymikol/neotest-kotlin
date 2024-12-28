import io.kotest.core.spec.style.DescribeSpec

class CoolTest :
    DescribeSpec({

        describe("a cool test") {

            it("should do something cool") {
            }
        }
    })

class FunTests :
    FunSpec({
        test("String length should return the length of the string") {
            "sammy".length shouldBe 5
            "".length shouldBe 0
        }
        context("this outer block is enabled") {
            xtest("this test is disabled") {
                // test here
            }
        }
        xcontext("this block is disabled") {
            test("disabled by inheritance from the parent") {
                // test here
            }
        }
    })
