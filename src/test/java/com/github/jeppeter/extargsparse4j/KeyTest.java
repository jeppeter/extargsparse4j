package com.github.jeppeter.extargsparse4j;

//import java.util.regex.*;
import com.github.jeppeter.extargsparse4j.KeyException;
import com.github.jeppeter.extargsparse4j.Key;
import com.github.jeppeter.jsonext.*;
//import org.json.simple.JSONObject;
//import org.json.simple.JSONArray;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import static org.junit.Assert.assertEquals;
import org.junit.*;

public class KeyTest {

    private Logger m_logger;

    private void __get_logger() {
        if (this.m_logger == null) {
            this.m_logger = LogManager.getLogger(this.getClass().getName());
        }
        assert(this.m_logger != null);
    }

    private void assert_string_value(Key flags, String keyname, String value)
    throws NoSuchFieldException, KeyException, IllegalAccessException {
        this.__get_logger();
        assertEquals(String.format("(%s)%s", flags.get_string_value("origkey"), keyname), flags.get_string_value(keyname), value);
        return;
    }

    private void assert_bool_value(Key flags, String keyname, Boolean bval)
    throws NoSuchFieldException, KeyException, IllegalAccessException {
        this.__get_logger();
        assertEquals(String.format("(%s)%s", flags.get_string_value("origkey"), keyname), flags.get_bool_value(keyname), bval);
        return;
    }

    private void assert_object_value(Key flags, String keyname, Object oval)
    throws NoSuchFieldException, KeyException, IllegalAccessException {
        this.__get_logger();
        assertEquals(String.format("(%s)%s", flags.get_string_value("origkey"), keyname), flags.get_object_value(keyname), oval);
        return;
    }

    private void assert_not_get(Key flags, String keyname)
    throws NoSuchFieldException, KeyException, IllegalAccessException {
        Boolean ok = false;
        String sval;
        try {
            sval = flags.get_string_value(keyname);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("(%s)%s no value", flags.get_string_value("origkey"), keyname), ok, true);
        return;
    }

    private void __opt_fail_check(Key flags) throws NoSuchFieldException, KeyException, IllegalAccessException {
        this.assert_not_get(flags, "longopt");
        this.assert_not_get(flags, "shortopt");
        this.assert_not_get(flags, "optdest");
        return;
    }

    @Before
    public void Setup() {
        if (this.m_logger == null) {
            this.m_logger = LogManager.getLogger(this.getClass().getName());
        }
    }

    @Test
    public void test_A001()  throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags = new Key("", "$flag|f+type", "string", false);
        Object sobj = (Object) "string";
        this.assert_string_value(flags, "flagname", "flag");
        this.assert_string_value(flags, "longopt", "--type-flag");
        this.assert_string_value(flags, "shortopt", "-f");
        this.assert_string_value(flags, "optdest", "type_flag");
        this.assert_object_value(flags, "value", sobj);
        this.assert_string_value(flags, "type", "string");
        this.assert_string_value(flags, "shortflag", "f");
        this.assert_string_value(flags, "prefix", "type");
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "helpinfo", null);
        this.assert_string_value(flags, "function", null);
        this.assert_bool_value(flags, "isflag", true);
        this.assert_bool_value(flags, "iscmd", false);
        return;
    }

    @Test
    public void test_A002() throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        JsonExt jsonext  = new JsonExt();
        Key flags ;
        Object aobj;

        jsonext.parseString("{\"array\" : []}");
        aobj = jsonext.getObject("/array");
        flags = new Key("", "$flag|f+type", aobj, true);
        this.assert_string_value(flags, "flagname", "flag");
        this.assert_string_value(flags, "shortflag", "f");
        this.assert_string_value(flags, "prefix", "type");
        this.assert_string_value(flags, "longopt", "--type-flag");
        this.assert_string_value(flags, "shortopt", "-f");
        this.assert_string_value(flags, "optdest", "type_flag");
        this.assert_string_value(flags, "helpinfo", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "cmdname", null);
        this.assert_bool_value(flags, "isflag", true);
        this.assert_bool_value(flags, "iscmd", false);
        return;
    }

    @Test
    public void test_A003() throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        JsonExt jsonext = new JsonExt();
        Key flags;
        Object bobj;
        jsonext.parseString("{\"bool\" : false}");
        bobj = jsonext.getObject("/bool");
        flags = new Key("", "flag|f", bobj, false);
        this.assert_string_value(flags, "flagname", "flag");
        this.assert_string_value(flags, "shortflag", "f");
        this.assert_string_value(flags, "prefix", "");
        this.assert_string_value(flags, "longopt", "--flag");
        this.assert_string_value(flags, "shortopt", "-f");
        this.assert_string_value(flags, "optdest", "flag");
        this.assert_object_value(flags, "value", bobj);
        this.assert_string_value(flags, "type", "bool");
        this.assert_string_value(flags, "prefix", "");
        this.assert_string_value(flags, "helpinfo", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "cmdname", null);
        this.assert_bool_value(flags, "isflag", true);
        this.assert_bool_value(flags, "iscmd", false);
        return;
    }

    @Test
    public void test_A004() throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        JsonExt jsonext = new JsonExt();
        Key flags;
        Object dobj;
        jsonext.parseString("{\"dict\" : {}}");
        dobj = jsonext.getObject("/dict");
        flags = new Key("newtype", "flag<flag.main>##help for flag##", dobj, false);
        this.assert_string_value(flags, "cmdname", "flag");
        this.assert_string_value(flags, "function", "flag.main");
        this.assert_string_value(flags, "type", "command");
        this.assert_string_value(flags, "prefix", "flag");
        this.assert_string_value(flags, "helpinfo", "help for flag");
        this.assert_string_value(flags, "flagname", null);
        this.assert_string_value(flags, "shortflag", null);
        this.assert_object_value(flags, "value", dobj);
        this.assert_bool_value(flags, "isflag", false);
        this.assert_bool_value(flags, "iscmd", true);
        this.__opt_fail_check(flags);
        return;
    }

    @Test
    public void test_A005()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean ok = false;
        try {
            flags = new Key("", "flag<flag.main>##help for flag##", "", true);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("not pass flags ok"), ok, true);
    }

    @Test
    public void test_A006()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        JsonExt jsonext = new JsonExt();
        Object dobj;

        jsonext.parseString("{\"new\": false}");
        dobj = jsonext.getObject("/");
        flags = new Key("", "flag+type<flag.main>##main", dobj, false);
        this.assert_string_value(flags, "cmdname", "flag");
        this.assert_string_value(flags, "prefix", "flag");
        this.assert_string_value(flags, "function", "flag.main");
        this.assert_string_value(flags, "helpinfo", null);
        this.assert_string_value(flags, "flagname", null);
        this.assert_string_value(flags, "shortflag", null);
        this.assert_bool_value(flags, "isflag", false);
        this.assert_bool_value(flags, "iscmd", true);
        this.assert_string_value(flags, "type", "command");
        this.assert_object_value(flags, "value", dobj);
        this.__opt_fail_check(flags);
        return;
    }

    @Test
    public void test_A007()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        JsonExt jsonext = new JsonExt();
        Object dobj;

        jsonext.parseString("{}");
        dobj = jsonext.getObject("/");
        flags = new Key("", "+flag", dobj, false);
        this.assert_string_value(flags, "prefix", "flag");
        this.assert_object_value(flags, "value", dobj);
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "shortflag", null);
        this.assert_string_value(flags, "flagname", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "helpinfo", null);
        this.assert_bool_value(flags, "isflag", true);
        this.assert_bool_value(flags, "iscmd", false);
        this.assert_string_value(flags, "type", "prefix");
        this.__opt_fail_check(flags);
        return;
    }

    @Test
    public void test_A008()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean ok = false;
        try {
            flags = new Key("", "+flag## help ##", null, false);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("pass not ok"), ok, true);
        return;
    }

    @Test
    public void test_A009()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean ok = false;
        try {
            flags = new Key("", "+flag<flag.main>", null, false);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("pass not ok"), ok, true);
        return;
    }

    @Test
    public void test_A010()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean ok = false;
        try {
            flags = new Key("", "flag|f2", null, false);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("pass not ok"), ok, true);
        return;
    }

    @Test
    public void test_A011()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean ok = false;
        try {
            flags = new Key("", "f|f2", null, false);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("pass not ok"), ok, true);
        return;
    }

    @Test
    public void test_A012()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean ok = false;
        JsonExt jsonext = new JsonExt();
        Object dobj;
        jsonext.parseString("{}");
        dobj = jsonext.getObject("/");
        try {
            flags = new Key("", "$flag|f<flag.main>", dobj, false);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("pass not ok"), ok, true);
        return;
    }

    @Test
    public void test_A013()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean ok = false;
        try {
            flags = new Key("", "$flag|f+cc<flag.main>", null, false);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("pass not ok"), ok, true);
        return;
    }

    @Test
    public void test_A014()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean ok = false;
        try {
            flags = new Key("", "c$", "", false);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("pass not ok"), ok, true);
        return;
    }

    @Test
    public void test_A015()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean ok = false;
        try {
            flags = new Key("", "$$", "", false);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("pass not ok"), ok, true);
        return;
    }

    @Test
    public void test_A016()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        JsonExt jsonext = new JsonExt();
        Object dobj;
        jsonext.parseString("{\"nargs\" : \"+\"}");
        dobj = jsonext.getObject("/");
        flags = new Key("", "$", dobj, false);
        this.assert_string_value(flags, "flagname", "$");
        this.assert_string_value(flags, "prefix", "");
        this.assert_string_value(flags, "type", "args");
        this.assert_object_value(flags, "value", null);
        this.assert_string_value(flags, "nargs", "+");
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "shortflag", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "helpinfo", null);
        this.assert_bool_value(flags, "isflag", true);
        this.assert_bool_value(flags, "iscmd", false);
        this.__opt_fail_check(flags);
        return;
    }

    @Test
    public void test_A017()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        JsonExt jsonext = new JsonExt();
        Object dobj;
        Double dblval;
        jsonext.parseString("{\"float\" : 3.3}");
        dobj = jsonext.getObject("/float");
        dblval = 3.3;
        flags = new Key("type", "flag+app## flag help ##", dobj, false);
        this.assert_string_value(flags, "flagname", "flag");
        this.assert_string_value(flags, "prefix", "type_app");
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "shortflag", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "type", "float");
        this.assert_object_value(flags, "value", dblval);
        this.assert_string_value(flags, "longopt", "--type-app-flag");
        this.assert_string_value(flags, "shortopt", null);
        this.assert_string_value(flags, "optdest", "type_app_flag");
        this.assert_string_value(flags, "helpinfo", " flag help ");
        this.assert_bool_value(flags, "isflag", true);
        this.assert_bool_value(flags, "iscmd", false);
        return;
    }

    @Test
    public void test_A018()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        JsonExt jsonext = new JsonExt();
        Object dobj;
        jsonext.parseString("{}");
        dobj = jsonext.getObject("/");
        flags = new Key("", "flag+app<flag.main>## flag help ##", dobj, false);
        this.assert_string_value(flags, "flagname", null);
        this.assert_string_value(flags, "prefix", "flag");
        this.assert_string_value(flags, "cmdname", "flag");
        this.assert_string_value(flags, "shortflag", null);
        this.assert_string_value(flags, "type", "command");
        this.assert_object_value(flags, "value", dobj);
        this.assert_string_value(flags, "function", "flag.main");
        this.assert_string_value(flags, "helpinfo", " flag help ");
        this.assert_bool_value(flags, "isflag", false);
        this.assert_bool_value(flags, "iscmd", true);
        this.__opt_fail_check(flags);
        return;
    }

    @Test
    public void test_A019()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        JsonExt jsonext = new JsonExt();
        Object dobj;
        jsonext.parseString("{\"prefix\":\"good\",\"value\":false}");
        dobj = jsonext.getObject("/");
        flags = new Key("", "$flag## flag help ##", dobj, false);
        this.assert_string_value(flags, "flagname", "flag");
        this.assert_string_value(flags, "prefix", "good");
        this.assert_object_value(flags, "value", false);
        this.assert_string_value(flags, "type", "bool");
        this.assert_string_value(flags, "helpinfo", " flag help ");
        this.assert_string_value(flags, "nargs", "0");
        this.assert_string_value(flags, "shortflag", null);
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "longopt", "--good-flag");
        this.assert_string_value(flags, "shortopt", null);
        this.assert_string_value(flags, "optdest", "good_flag");
        this.assert_bool_value(flags, "isflag", true);
        this.assert_bool_value(flags, "iscmd", false);
        return;
    }

    @Test
    public void test_A020()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean ok = false;
        try {
            flags = new Key("", "$", null, false);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("pass not ok"), ok, true);
        return;
    }

    @Test
    public void test_A021()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        JsonExt jsonext = new JsonExt();
        Object dobj;
        jsonext.parseString("{\"nargs\" : \"?\",\"value\":null}");
        dobj = jsonext.getObject("/");
        flags = new Key("command", "$## self define ##", dobj, false);
        this.assert_bool_value(flags, "iscmd", false);
        this.assert_bool_value(flags, "isflag", true);
        this.assert_string_value(flags, "prefix", "command");
        this.assert_string_value(flags, "flagname", "$");
        this.assert_string_value(flags, "shortflag", null);
        this.assert_object_value(flags, "value", null);
        this.assert_string_value(flags, "type", "args");
        this.assert_string_value(flags, "nargs", "?");
        this.assert_string_value(flags, "helpinfo", " self define ");
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "function", null);
        this.__opt_fail_check(flags);
        return;
    }

    @Test
    public void test_A022()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        JsonExt jsonext = new JsonExt();
        Object dobj;
        jsonext.parseString("{}");
        dobj = jsonext.getObject("/");
        flags = new Key("command", "+flag", dobj, false);
        this.assert_bool_value(flags, "iscmd", false);
        this.assert_string_value(flags, "prefix", "command_flag");
        this.assert_object_value(flags, "value", dobj);
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "shortflag", null);
        this.assert_string_value(flags, "flagname", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "helpinfo", null);
        this.assert_bool_value(flags, "isflag", true);
        this.assert_string_value(flags, "type", "prefix");
        this.__opt_fail_check(flags);
        return;
    }

    @Test
    public void test_A023()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        JsonExt jsonext = new JsonExt();
        Object dobj;
        Double dblval = 3.9;
        jsonext.parseString("{\"prefix\":\"good\",\"value\":3.9,\"nargs\":1}");
        dobj = jsonext.getObject("/");
        flags = new Key("", "$flag## flag help ##", dobj, false);
        this.assert_string_value(flags, "flagname", "flag");
        this.assert_string_value(flags, "prefix", "good");
        this.assert_object_value(flags, "value", dblval);
        this.assert_string_value(flags, "type", "float");
        this.assert_string_value(flags, "helpinfo", " flag help ");
        this.assert_string_value(flags, "nargs", "1");
        this.assert_string_value(flags, "shortflag", null);
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "longopt", "--good-flag");
        this.assert_string_value(flags, "shortopt", null);
        this.assert_string_value(flags, "optdest", "good_flag");
        this.assert_bool_value(flags, "iscmd", false);
        this.assert_bool_value(flags, "isflag", true);
        return;
    }

    @Test
    public void test_A024()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean ok = false;
        JsonExt jsonext = new JsonExt();
        Object dobj;
        jsonext.parseString("{\"prefix\":\"good\",\"value\":false,\"nargs\":2}");
        dobj = jsonext.getObject("/");
        try {
            flags = new Key("", "$flag## flag help ##", dobj, false);
        } catch (KeyException e) {
            ok = true;
        }
        assertEquals(String.format("pass not ok"), ok, true);
        return;
    }


    @Test
    public void test_A026()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        flags = new Key("dep", "$", "+", true);
        this.assert_string_value(flags, "flagname", "$");
        this.assert_string_value(flags, "prefix", "dep");
        this.assert_object_value(flags, "value", null);
        this.assert_string_value(flags, "type", "args");
        this.assert_string_value(flags, "helpinfo", null);
        this.assert_string_value(flags, "nargs", "+");
        this.assert_string_value(flags, "shortflag", null);
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "function", null);
        this.__opt_fail_check(flags);
        return;
    }

    @Test
    public void test_A027()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Long lobj = new Long(new Integer(0));
        flags = new Key("dep", "verbose|v", "+", false);
        this.assert_string_value(flags, "flagname", "verbose");
        this.assert_string_value(flags, "shortflag", "v");
        this.assert_string_value(flags, "prefix", "dep");
        this.assert_string_value(flags, "type", "count");
        this.assert_object_value(flags, "value", lobj);
        this.assert_string_value(flags, "helpinfo", null);
        this.assert_string_value(flags, "nargs", "0");
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "optdest", "dep_verbose");
        this.assert_string_value(flags, "longopt", "--dep-verbose");
        this.assert_string_value(flags, "shortopt", "-v");
        return;
    }

    @Test
    public void test_A028()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Long lobj = new Long(new Integer(0));
        flags = new Key("", "verbose|v## new help info ##", "+", false);
        this.assert_string_value(flags, "flagname", "verbose");
        this.assert_string_value(flags, "shortflag", "v");
        this.assert_string_value(flags, "prefix", "");
        this.assert_string_value(flags, "type", "count");
        this.assert_object_value(flags, "value", lobj);
        this.assert_string_value(flags, "helpinfo", " new help info ");
        this.assert_string_value(flags, "nargs", "0");
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "optdest", "verbose");
        this.assert_string_value(flags, "longopt", "--verbose");
        this.assert_string_value(flags, "shortopt", "-v");
        return;
    }

    @Test
    public void test_A029()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Boolean bval = true;
        flags = new Key("", "rollback|R## rollback not set ##", true, false);
        this.assert_string_value(flags, "flagname", "rollback");
        this.assert_string_value(flags, "shortflag", "R");
        this.assert_string_value(flags, "prefix", "");
        this.assert_string_value(flags, "type", "bool");
        this.assert_object_value(flags, "value", bval);
        this.assert_string_value(flags, "helpinfo", " rollback not set ");
        this.assert_string_value(flags, "nargs", "0");
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "optdest", "rollback");
        this.assert_string_value(flags, "longopt", "--no-rollback");
        this.assert_string_value(flags, "shortopt", "-R");
        return;
    }

    @Test
    public void test_A030()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Long lobj = new Long(0xffffffff);
        flags = new Key("", "maxval|m##max value set ##", lobj, false);
        this.assert_string_value(flags, "flagname", "maxval");
        this.assert_string_value(flags, "shortflag", "m");
        this.assert_string_value(flags, "prefix", "");
        this.assert_string_value(flags, "type", "long");
        this.assert_object_value(flags, "value", lobj);
        this.assert_string_value(flags, "helpinfo", "max value set ");
        this.assert_string_value(flags, "nargs", "1");
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "optdest", "maxval");
        this.assert_string_value(flags, "longopt", "--maxval");
        this.assert_string_value(flags, "shortopt", "-m");
        return;
    }

    @Test
    public void test_A031()throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Object dobj;
        JsonExt jsonext = new JsonExt();
        jsonext.parseString("{\"array\" : [\"maxval\"]}");
        dobj = jsonext.getObject("/array");
        flags = new Key("", "maxval|m", dobj, false);
        this.assert_string_value(flags, "flagname", "maxval");
        this.assert_string_value(flags, "shortflag", "m");
        this.assert_string_value(flags, "prefix", "");
        this.assert_string_value(flags, "type", "list");
        this.assert_object_value(flags, "value", dobj);
        this.assert_string_value(flags, "helpinfo", null);
        this.assert_string_value(flags, "nargs", "1");
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "optdest", "maxval");
        this.assert_string_value(flags, "longopt", "--maxval");
        this.assert_string_value(flags, "shortopt", "-m");
        return;
    }

    @Test
    public void test_A032() throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Object dobj;
        JsonExt jsonext = new JsonExt();
        jsonext.parseString("{\"value\":[],\"type\":\"string\",\"nargs\":\"*\"}");
        dobj = jsonext.getObject("/");
        flags = new Key("", "$", dobj, false);
        this.assert_string_value(flags, "flagname", "$");
        this.assert_string_value(flags, "shortflag", null);
        this.assert_string_value(flags, "prefix", "");
        this.assert_string_value(flags, "type", "args");
        dobj = jsonext.getObject("/value");
        this.assert_object_value(flags, "value", dobj);
        this.assert_string_value(flags, "nargs", "*");
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "function", null);
        this.__opt_fail_check(flags);
        return;
    }

    @Test
    public void test_A033()  throws NoSuchFieldException, KeyException, IllegalAccessException,
        JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        Key flags;
        Long lobj = new Long(new Integer(0));
        flags = new Key("", "verbose|v##increment verbose mode##", "+", false);
        this.assert_string_value(flags, "flagname", "verbose");
        this.assert_string_value(flags, "shortflag", "v");
        this.assert_string_value(flags, "prefix", "");
        this.assert_string_value(flags, "type", "count");
        this.assert_object_value(flags, "value", lobj);
        this.assert_string_value(flags, "helpinfo", "increment verbose mode");
        this.assert_string_value(flags, "nargs", "0");
        this.assert_string_value(flags, "cmdname", null);
        this.assert_string_value(flags, "function", null);
        this.assert_string_value(flags, "optdest", "verbose");
        this.assert_string_value(flags, "longopt", "--verbose");
        this.assert_string_value(flags, "shortopt", "-v");

        this.m_logger.info(String.format("flags %s",flags.toString()));
        return;
    }
}