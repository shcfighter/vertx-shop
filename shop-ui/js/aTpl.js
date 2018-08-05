/**
 * js template
 * @date   2016-03-14
 * @author ganzw@guahao.com
 * @site   https://github.com/baixuexiyang/aTpl.git
 */
;(function (name, fun) {
    if(typeof module !== 'undefined' && module.exports) {
        module.exports = fun();
    } else if(typeof define === 'function' && define.amd) {
        define(fun);   
    }else {
        this[name] = fun();
    }
})('aTpl', function () {
    "use strict";
    var aTpl = {
        version: '1.0.0',
        open: "{{",
        close: "}}",
        config: function(options) {
            this.open = options.open || '{{';
            this.close = options.close || '}}';
            return this;
        },
        exper: function(str){
            return new RegExp(str, 'g');
        },
        seperate: function(t, _, __){
            // seperate html and javscript
            // use the regep @([\\s\\S])+? to seperate javascript
            // and ([^{@}])*? is html
            var tmp = ['@([\\s\\S])+?', '([^{@}])*?'][t || 0];
            return this.exper((_||'') + this.open + tmp + this.close + (__||''));
        },   
        escape: function(html){
            // convert symbol to  
            return String(html||'').replace(/&(?!#?[a-zA-Z0-9]+;)/g, '&amp;')
            .replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/'/g, '&#39;').replace(/"/g, '&quot;');
        },
        error: function(e){
            typeof console != "undefined" && typeof console.error != "undefined" && console.error(e);
            return e;
        },
        parser: function(tpl, data) {
            var _this = this,
                open = _this.exper('^'+_this.open+'@', ''),
                end = _this.exper(_this.close+'$', '');
            this.bk = tpl;
            tpl = tpl
                // replace space
                .replace(/[\r\t\n]/g, ' ')
                // delete unuse space
                .replace(_this.exper(_this.open+'@'), _this.open+'@ ')
                .replace(_this.exper(_this.close+'}'), '} '+_this.close)
                .replace(/\\/g, '\\\\')
                .replace(/(?="|')/g, '\\')
                // javascript
                .replace(_this.seperate(), function(str){
                    str = str.replace(open, '').replace(end, '');
                    return '";' + str.replace(/\\/g, '') + '; _v+="';
                })
                // html
                .replace(_this.seperate(1), function(str){
                    var open = '"+(';
                    if(str.replace(/\s/g, '') === _this.open+_this.close){
                        return '';
                    }
                    str = str.replace(_this.exper(_this.open+'|'+_this.close), '');
                    if(/^=/.test(str)){
                        str = str.replace(/^=/, '');
                        open = '"+_escape_(';
                    }
                    return open + str.replace(/\\/g, '') + ')+"';
                });
            tpl = '"use strict";var _v = "' + tpl + '";return _v;';
            try{
                _this.cache = tpl = new Function('aTpl, _escape_', tpl);
                return tpl(data, _this.escape);
            } catch(e){
                delete _this.cache;
                return _this.error(e + _this.tpl);
            }
        },
        template: function(tpl) {
            if(typeof tpl === 'string') {
                this.tpl = tpl;
                return this;
            }
            tpl = tpl[0] || tpl;
            if(tpl.type !== 'text/aTpl') { 
                return this;
            }
            this.tpl = tpl.innerHTML;
            return this;
        },
        render: function(data, cb) {
            typeof data === 'function' && (data = data());
            var _this = this, tpl;
            if(!data) return _this.error('no data');
            if(!this.tpl) return _this.error('no template');
            tpl = (_this.cache && _this.tpl === _this.bk) ? _this.cache(data, _this.escape) : _this.parser(_this.tpl, data);
            if(!cb) return tpl;
            cb(tpl);
        }
    };
    return aTpl;
});
