package com.codymikol.gummibear

class DescribeTest : DescribeSpec({
describe("some namespace") {

  it("some test") {

  }

  xit("some disabled test") {

  }

}

xdescribe("some disabled namespace") {

  it("some test") {

  }

}

describe("some parent namespace") {

  describe("some child namespace") {
    
    it("some grandchild test") {

    }

  }

}
})
