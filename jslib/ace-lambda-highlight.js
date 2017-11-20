define('ace/mode/lambda', (require, exports, module) => {
    let oop = require('ace/lib/oop'),
        TextMode = require("ace/mode/text").Mode,
        TextHighlightRules = require("ace/mode/text_highlight_rules").TextHighlightRules

    let LambdaHighlight = function() {
        this.$rules = {
            "start": [{
                token: ["entity.name.function", "keyword.operator"],
                regex: "(.+?)(=)",
                next: "start"
            }, {
                regex: "(\\\\|λ)([^.]+)(\.)",
                token: ["keyword", "variable", "keyword.operator"]
            }, {
                regex: "\\b\\d+\\b",
                token: "constant.numeric"
            }]
        }
    }
    oop.inherits(LambdaHighlight, TextHighlightRules)
    
    let Mode = function() {
        this.HighlightRules = LambdaHighlight
    }
    oop.inherits(Mode, TextMode)
    exports.Mode = Mode
})
