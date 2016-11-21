package com.github.jeppeter.extargsparse4j;

//import java.util.regex.*;
import com.github.jeppeter.extargsparse4j.ParserException;
import com.github.jeppeter.extargsparse4j.Parser;
import com.github.jeppeter.extargsparse4j.NameSpaceEx;
import com.github.jeppeter.extargsparse4j.Environ;
import com.github.jeppeter.extargsparse4j.Priority;

import com.github.jeppeter.jsonext.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;


import static org.junit.Assert.assertEquals;
import org.junit.*;

public class ParserTest {
    private Logger m_logger;

    @Before
    public void Setup() {

        this.__get_logger();

    }


    private void __get_logger() {
        if (this.m_logger == null) {
            this.m_logger = LogManager.getLogger(this.getClass().getName());
        }
    }

    private void assert_string_value(NameSpaceEx args, String key, String value) {
        assertEquals(String.format("%s=%s", key, value), (String)args.get(key), value);
        return;
    }

    private void assert_long_value(NameSpaceEx args, String key, Long value) {
        //this.m_logger.info(String.format("%s=%s %s",key,value.toString(),args.get(key).toString()));
        assertEquals(String.format("%s=%d", key, value), (Long)args.get(key), value);
        return;
    }

    private void assert_bool_value(NameSpaceEx args, String key, Boolean value) {
        assertEquals(String.format("%s=%s", key, value.toString()), (Boolean)args.get(key), value);
        return;
    }

    private void assert_object_value(NameSpaceEx args, String key, Object value) {
        assertEquals(String.format("%s=%s", key, value.toString()), args.get(key), value);
        return;
    }

    private void assert_list_value(NameSpaceEx args, String key, String value) throws JsonExtNotParsedException, JsonExtNotFoundException, JsonExtInvalidTypeException {
        JsonExt jext = new JsonExt();
        String parsevalue = String.format("{\"dummy\": %s}", value);
        Object dobj;
        jext.parseString(parsevalue);
        dobj = jext.getObject("/dummy");
        this.assert_object_value(args, key, dobj);
        return;
    }

    private void __unset_environs(String[] envkey) {
        for (String c : envkey) {
            Environ.unsetenv(c);
        }
        return;
    }

    private String __write_temp_file(String pattern, String suffix, String content) {
        String retfilename = null;
        while (true) {
            retfilename = null;
            try {
                File temp = File.createTempFile(pattern, suffix);
                FileOutputStream outf;
                retfilename = temp.getAbsolutePath();
                outf = new FileOutputStream(retfilename);
                outf.write(content.getBytes());
                outf.close();
                return retfilename;
            } catch (IOException e) {
                ;
            }
        }
    }

    public static NameSpaceEx call_args_function(NameSpaceEx args, Object ctx) {
        args.set("has_called_args", args.get("subcommand"));
        return args;
    }

    @Test
    public void test_A001() throws Exception {
        String loads = "{"
                       + "\"verbose|v##increment verbose mode##\" : \"+\",\n"
                       + "\"flag|f## flag set##\" : false,\n"
                       + "\"list|l\" : [],\n"
                       + "\"string|s\" : \"string_var\",\n"
                       + "\"$\" : {\n"
                       +     "\"value\" : [],\n"
                       +     "\"nargs\" : \"*\",\n"
                       +     "\"type\" : \"string\"\n"
                       + "},\n"
                       + "\"number|n\" : 0\n"
                       + "}\n";
        Parser parser ;
        NameSpaceEx args;
        String[] params = {"-vvvv", "-f", "-n", "30", "-l", "bar1", "-l", "bar2", "var1", "var2"};
        parser = new Parser();
        parser.load_command_line_string(loads);
        args = parser.parse_command_line(params);
        this.assert_long_value(args, "verbose", new Long(4));
        this.assert_bool_value(args, "flag", new Boolean(true));
        this.assert_long_value(args, "number", new Long(30));
        this.assert_list_value(args, "list", "[\"bar1\",\"bar2\"]");
        this.assert_list_value(args, "args", "[\"var1\",\"var2\"]");
        return;
    }

    @Test
    public void test_A002() throws Exception {
        String loads = "{"
                       + " \"verbose|v\" : \"+\",\n"
                       + "     \"dep\" : {\n"
                       + "          \"list|l\" : [],\n"
                       + "          \"string|s\" : \"s_var\",\n"
                       + "          \"$\" : \"+\"\n"
                       + "          \"master|m\" : [],\n"
                       + "    },"
                       + "\"port|p\" : 3000\n"
                       + "}";
        Parser parser;
        NameSpaceEx args;
        String[] params = {"-vvvv", "-p", "5000", "dep", "--dep-list", "arg1", "--dep-list", "arg2", "cc", "dd"};
        parser = new Parser();
        parser.load_command_line_string(loads);
        args = parser.parse_command_line(params);
        this.assert_long_value(args, "verbose", new Long(4));
        this.assert_long_value(args, "port", new Long(5000));
        this.assert_string_value(args, "subcommand", "dep");
        this.assert_list_value(args, "dep_list", "[\"arg1\",\"arg2\"]");
        this.assert_string_value(args, "dep_string", "s_var");
        this.assert_list_value(args, "subnargs", "[\"cc\",\"dd\"]");
        return;
    }

    @Test
    public void test_A003() throws Exception {
        String loads = "{"
                       + "       \"verbose|v\" : \"+\",\n"
                       + "       \"port|p\" : 3000,\n"
                       + "       \"dep\" : {\n"
                       + "              \"list|l\" : [],\n"
                       + "              \"string|s\" : \"s_var\",\n"
                       + "              \"$\" : \"+\"\n"
                       + "       },\n"
                       + "       \"rdep\" : {\n"
                       + "              \"list|L\" : [],\n"
                       + "              \"string|S\" : \"s_rdep\",\n"
                       + "              \"$\" : 2\n"
                       + "       }\n"
                       + "}\n";
        Parser parser ;
        String[] params = {"-vvvv", "-p", "5000", "rdep", "-L", "arg1", "--rdep-list", "arg2", "cc", "dd"};
        NameSpaceEx args;
        parser = new Parser();
        parser.load_command_line_string(loads);
        args = parser.parse_command_line(params);
        this.assert_long_value(args, "verbose", new Long(4));
        this.assert_long_value(args, "port", new Long(5000));
        this.assert_string_value(args, "subcommand", "rdep");
        this.assert_list_value(args, "rdep_list", "[\"arg1\",\"arg2\"]");
        this.assert_string_value(args, "rdep_string", "s_rdep");
        this.assert_list_value(args, "subnargs", "[\"cc\",\"dd\"]");
        return;
    }

    @Test
    public void test_A004() throws Exception {
        String loads = "{\n"
                       + "    \"verbose|v\" : \"+\",\n"
                       + "    \"port|p\" : 3000,\n"
                       + "    \"dep\" : {\n"
                       + "        \"list|l\" : [],\n"
                       + "        \"string|s\" : \"s_var\",\n"
                       + "        \"$\" : \"+\"\n"
                       + "    },\n"
                       + "    \"rdep\" : {\n"
                       + "        \"list|L\" : [],\n"
                       + "        \"string|S\" : \"s_rdep\",\n"
                       + "        \"$\" : 2\n"
                       + "    }\n"
                       + "}\n";
        Parser parser;
        String[] params = {"-vvvv", "-p", "5000", "rdep", "-L", "arg1", "--rdep-list", "arg2", "cc", "dd"};
        NameSpaceEx args;
        parser = new Parser();
        parser.load_command_line_string(loads);
        args = parser.parse_command_line(params);
        this.assert_long_value(args, "verbose", new Long(4));
        this.assert_long_value(args, "port", new Long(5000));
        this.assert_string_value(args, "subcommand", "rdep");
        this.assert_list_value(args, "rdep_list", "[\"arg1\",\"arg2\"]");
        this.assert_string_value(args, "rdep_string", "s_rdep");
        this.assert_list_value(args, "subnargs", "[\"cc\",\"dd\"]");
        return;
    }

    @Test
    public void test_A005() throws Exception {
        String formats = "{\n"
                         + "    \"verbose|v\" : \"+\",\n"
                         + "    \"port|p\" : 3000,\n"
                         + "    \"dep<%s.call_args_function>\" : {\n"
                         + "        \"list|l\" : [],\n"
                         + "        \"string|s\" : \"s_var\",\n"
                         + "        \"$\" : \"+\"\n"
                         + "    },\n"
                         + "    \"rdep\" : {\n"
                         + "        \"list|L\" : [],\n"
                         + "        \"string|S\" : \"s_rdep\",\n"
                         + "        \"$\" : 2\n"
                         + "    }\n"
                         + "}\n";
        String loads = String.format(formats, this.getClass().getName());
        Parser parser;
        String[] params = {"-p", "7003", "-vvvvv", "dep", "-l", "foo1", "-s", "new_var", "zz"};
        NameSpaceEx args;
        parser = new Parser();
        parser.load_command_line_string(loads);
        args = parser.parse_command_line(params, parser);
        this.assert_long_value(args, "verbose", new Long(5));
        this.assert_long_value(args, "port", new Long(7003));
        this.assert_string_value(args, "subcommand", "dep");
        this.assert_list_value(args, "dep_list", "[\"foo1\"]");
        this.assert_string_value(args, "dep_string", "new_var");
        this.assert_list_value(args, "subnargs", "[\"zz\"]");
        this.assert_string_value(args, "has_called_args", args.getString("subcommand"));
        return;
    }

    @Test
    public void test_A006() throws Exception {
        String loads1 = "{\n"
                        + "    \"verbose|v\" : \"+\",\n"
                        + "    \"port|p\" : 3000,\n"
                        + "    \"dep\" : {\n"
                        + "        \"list|l\" : [],\n"
                        + "        \"string|s\" : \"s_var\",\n"
                        + "        \"$\" : \"+\"\n"
                        + "    }\n"
                        + "}\n";
        String loads2 = "{\n"
                        + "    \"rdep\" : {\n"
                        + "        \"list|L\" : [],\n"
                        + "        \"string|S\" : \"s_rdep\",\n"
                        + "        \"$\" : 2\n"
                        + "    }\n"
                        + "}\n";
        Parser parser;
        String[] params = {"-p", "7003", "-vvvvv", "rdep", "-L", "foo1", "-S", "new_var", "zz", "64"};
        NameSpaceEx args;
        parser = new Parser();
        parser.load_command_line_string(loads1);
        parser.load_command_line_string(loads2);
        args = parser.parse_command_line(params, (Object)parser);
        this.assert_long_value(args, "verbose", new Long(5));
        this.assert_long_value(args, "port", new Long(7003));
        this.assert_string_value(args, "subcommand", "rdep");
        this.assert_list_value(args, "rdep_list", "[\"foo1\"]");
        this.assert_string_value(args, "rdep_string", "new_var");
        this.assert_list_value(args, "subnargs", "[\"zz\",\"64\"]");
        return;
    }

    @Test
    public void test_A007() throws Exception {
        String commandline = "{"
                             + "    \"verbose|v\" : \"+\",\n"
                             + "    \"port|p+http\" : 3000,\n"
                             + "    \"dep\" : {\n"
                             + "        \"list|l\" : [],\n"
                             + "        \"string|s\" : \"s_var\",\n"
                             + "        \"$\" : \"+\"\n"
                             + "    }\n"
                             + "}\n";
        Parser parser;
        NameSpaceEx args;
        String[] params = {"-vvvv", "dep", "-l", "cc", "--dep-string", "ee", "ww"};

        parser = new Parser();
        parser.load_command_line_string(commandline);
        args = parser.parse_command_line(params);
        this.assert_long_value(args, "verbose", new Long(4));
        this.assert_long_value(args, "http_port", new Long(3000));
        this.assert_string_value(args, "subcommand", "dep");
        this.assert_list_value(args, "dep_list", "[\"cc\"]");
        this.assert_string_value(args, "dep_string", "ee");
        this.assert_list_value(args, "subnargs", "[\"ww\"]");
        return;
    }

    @Test
    public void test_A008() throws Exception {
        String commandline = "{\n"
                             + "    \"verbose|v\" : \"+\",\n"
                             + "    \"+http\" : {\n"
                             + "        \"port|p\" : 3000,\n"
                             + "        \"visual_mode|V\" : false\n"
                             + "    },\n"
                             + "    \"dep\" : {\n"
                             + "        \"list|l\" : [],\n"
                             + "        \"string|s\" : \"s_var\",\n"
                             + "        \"$\" : \"+\"\n"
                             + "    }\n"
                             + "}";
        Parser parser;
        NameSpaceEx args;
        String[] params = {"-vvvv", "--http-port", "9000", "--http-visual-mode", "dep", "-l", "cc", "--dep-string", "ee", "ww"};
        parser = new Parser();
        parser.load_command_line_string(commandline);
        args = parser.parse_command_line(params);
        this.assert_long_value(args, "verbose", new Long(4));
        this.assert_long_value(args, "http_port", new Long(9000));
        this.assert_bool_value(args, "http_visual_mode", new Boolean(true));
        this.assert_string_value(args, "subcommand", "dep");
        this.assert_list_value(args, "dep_list", "[\"cc\"]");
        this.assert_string_value(args, "dep_string", "ee");
        this.assert_list_value(args, "subnargs", "[\"ww\"]");
        return;
    }

    @Test
    public void test_A009() throws Exception {
        String commandline = "{\n"
                             + "    \"verbose|v\" : \"+\",\n"
                             + "    \"$port|p\" : {\n"
                             + "        \"value\" : 3000,\n"
                             + "        \"type\" : \"int\",\n"
                             + "        \"nargs\" : 1 , \n"
                             + "        \"helpinfo\" : \"port to connect\"\n"
                             + "    },\n"
                             + "    \"dep\" : {\n"
                             + "        \"list|l\" : [],\n"
                             + "        \"string|s\" : \"s_var\",\n"
                             + "        \"$\" : \"+\"\n"
                             + "    }\n"
                             + "}\n";
        Parser parser;
        NameSpaceEx args;
        String[] params = {"-vvvv", "-p", "9000", "dep", "-l", "cc", "--dep-string", "ee", "ww"};
        parser = new Parser();
        parser.load_command_line_string(commandline);
        args = parser.parse_command_line(params);
        this.assert_long_value(args, "verbose", new Long(4));
        this.assert_long_value(args, "port", new Long(9000));
        this.assert_list_value(args, "dep_list", "[\"cc\"]");
        this.assert_string_value(args, "dep_string", "ee");
        this.assert_list_value(args, "subnargs", "[\"ww\"]");
        return;
    }

    @Test
    public void test_A010() throws Exception {
        String commandline = "{\n"
                             + "    \"verbose|v\" : \"+\",\n"
                             + "    \"$port|p\" : {\n"
                             + "        \"value\" : 3000,\n"
                             + "        \"type\" : \"int\",\n"
                             + "        \"nargs\" : 1 , \n"
                             + "        \"helpinfo\" : \"port to connect\"\n"
                             + "    },\n"
                             + "    \"dep\" : {\n"
                             + "        \"list|l\" : [],\n"
                             + "        \"string|s\" : \"s_var\",\n"
                             + "        \"$\" : \"+\"\n"
                             + "    }\n"
                             + "}\n";
        String depjsonfile = null;
        Parser parser = null;
        NameSpaceEx args;
        String[] params = {"-vvvv", "-p", "9000", "dep", "--dep-json", "depjsonfile", "--dep-string", "ee", "ww"};
        int i;

        try {
            depjsonfile = this.__write_temp_file("parse", ".json", "{\"list\" : [\"jsonval1\",\"jsonval2\"],\"string\" : \"jsonstring\"}\n");
            parser = new Parser();
            parser.load_command_line_string(commandline);
            for (i = 0; i < params.length; i++) {
                if (params[i].equals("depjsonfile")) {
                    params[i] = depjsonfile;
                    break;
                }
            }
            this.m_logger.info(String.format("depjsonfile %s", depjsonfile));
            args = parser.parse_command_line(params);
            this.assert_long_value(args, "verbose", new Long(4));
            this.assert_long_value(args, "port", new Long(9000));
            this.assert_string_value(args, "subcommand", "dep");
            this.assert_list_value(args, "dep_list", "[\"jsonval1\",\"jsonval2\"]");
            this.assert_string_value(args, "dep_string", "ee");
            this.assert_list_value(args, "subnargs", "[\"ww\"]");
        } finally {
            if (depjsonfile != null) {
                File file = new File(depjsonfile);
                file.delete();
                depjsonfile = null;
            }
        }
        return;
    }

    @Test
    public void test_A011() throws Exception {
        String commandline = "{\n"
                             + "    \"verbose|v\" : \"+\",\n"
                             + "    \"$port|p\" : {\n"
                             + "        \"value\" : 3000,\n"
                             + "        \"type\" : \"int\",\n"
                             + "        \"nargs\" : 1 , \n"
                             + "        \"helpinfo\" : \"port to connect\"\n"
                             + "    },\n"
                             + "    \"dep\" : {\n"
                             + "        \"list|l\" : [],\n"
                             + "        \"string|s\" : \"s_var\",\n"
                             + "        \"$\" : \"+\"\n"
                             + "    }\n"
                             + "}\n";
        String depjsonfile = null;
        String[] needenvs = {"EXTARGSPARSE_JSON", "DEP_JSON", "EXTARGS_VERBOSE", "EXTARGS_PORT", "DEP_LIST", "DEP_STRING"};
        String[] params = {"-vvvv", "-p", "9000", "dep", "--dep-string", "ee", "ww"};
        NameSpaceEx args;
        Parser parser;
        this.__unset_environs(needenvs);
        try {
            depjsonfile = this.__write_temp_file("parse", ".json", "{\"list\" : [\"jsonval1\",\"jsonval2\"],\"string\" : \"jsonstring\"}\n");
            parser = new Parser();
            parser.load_command_line_string(commandline);
            Environ.setenv("DEP_JSON", depjsonfile);
            args = parser.parse_command_line(params);
            this.assert_long_value(args, "verbose", new Long(4));
            this.assert_long_value(args, "port", new Long(9000));
            this.assert_string_value(args, "subcommand", "dep");
            this.assert_list_value(args, "dep_list", "[\"jsonval1\",\"jsonval2\"]");
            this.assert_string_value(args, "dep_string", "ee");
            this.assert_list_value(args, "subnargs", "[\"ww\"]");
        } finally {
            if (depjsonfile != null) {
                File file = new File(depjsonfile);
                file.delete();
                depjsonfile = null;
            }
        }
        return;
    }

    @Test
    public void test_A012() throws Exception {
        String commandline = "{\n"
                             + "    \"verbose|v\" : \"+\",\n"
                             + "    \"$port|p\" : {\n"
                             + "        \"value\" : 3000,\n"
                             + "        \"type\" : \"int\",\n"
                             + "        \"nargs\" : 1 , \n"
                             + "        \"helpinfo\" : \"port to connect\"\n"
                             + "    },\n"
                             + "    \"dep\" : {\n"
                             + "        \"list|l\" : [],\n"
                             + "        \"string|s\" : \"s_var\",\n"
                             + "        \"$\" : \"+\"\n"
                             + "    }\n"
                             + "}\n";
        Parser parser;
        String depjsonfile = null;
        String[] needenvs = {"EXTARGSPARSE_JSON", "DEP_JSON", "EXTARGS_VERBOSE", "EXTARGS_PORT", "DEP_LIST", "DEP_STRING"};
        String[] params = {"-p", "9000", "--json", "jsonfile", "dep", "--dep-string", "ee", "ww"};
        NameSpaceEx args;
        int i;
        this.__unset_environs(needenvs);
        try {
            depjsonfile = this.__write_temp_file("parse", ".json", "{\"dep\":{\"list\" : [\"jsonval1\",\"jsonval2\"],\"string\" : \"jsonstring\"},\"port\":6000,\"verbose\":3}\n");
            for (i = 0; i < params.length; i++) {
                if (params[i].equals("jsonfile")) {
                    params[i] = depjsonfile;
                    break;
                }
            }
            parser = new Parser();
            parser.load_command_line_string(commandline);
            args = parser.parse_command_line(params);
            this.assert_long_value(args, "verbose", new Long(3));
            this.assert_long_value(args, "port", new Long(9000));
            this.assert_string_value(args, "subcommand", "dep");
            this.assert_list_value(args, "dep_list", "[\"jsonval1\",\"jsonval2\"]");
            this.assert_string_value(args, "dep_string", "ee");
            this.assert_list_value(args, "subnargs", "[\"ww\"]");
        } finally {
            if (depjsonfile != null) {
                File file = new File(depjsonfile);
                file.delete();
                depjsonfile = null;
            }
        }
        return;
    }

    @Test
    public void test_A013() throws Exception {
        String commandline = "{\n"
                             + "    \"verbose|v\" : \"+\",\n"
                             + "    \"$port|p\" : {\n"
                             + "        \"value\" : 3000,\n"
                             + "        \"type\" : \"int\",\n"
                             + "        \"nargs\" : 1 , \n"
                             + "        \"helpinfo\" : \"port to connect\"\n"
                             + "    },\n"
                             + "    \"dep\" : {\n"
                             + "        \"list|l\" : [],\n"
                             + "        \"string|s\" : \"s_var\",\n"
                             + "        \"$\" : \"+\"\n"
                             + "    }\n"
                             + "}\n";
        Parser parser;
        String depjsonfile = null;
        String[] needenvs = {"EXTARGSPARSE_JSON", "DEP_JSON", "EXTARGS_VERBOSE", "EXTARGS_PORT", "DEP_LIST", "DEP_STRING"};
        String[] params = {"-p", "9000", "dep", "--dep-string", "ee", "ww"};
        NameSpaceEx args;
        this.__unset_environs(needenvs);
        try {
            depjsonfile = this.__write_temp_file("parse", ".json", "{\"dep\":{\"list\" : [\"jsonval1\",\"jsonval2\"],\"string\" : \"jsonstring\"},\"port\":6000,\"verbose\":3}\n");
            Environ.setenv("EXTARGSPARSE_JSON", depjsonfile);
            parser = new Parser();
            parser.load_command_line_string(commandline);
            args = parser.parse_command_line(params);
            this.assert_long_value(args, "verbose", new Long(3));
            this.assert_long_value(args, "port", new Long(9000));
            this.assert_string_value(args, "subcommand", "dep");
            this.assert_list_value(args, "dep_list", "[\"jsonval1\",\"jsonval2\"]");
            this.assert_string_value(args, "dep_string", "ee");
            this.assert_list_value(args, "subnargs", "[\"ww\"]");
        } finally {
            if (depjsonfile != null) {
                File file = new File(depjsonfile);
                file.delete();
                depjsonfile = null;
            }
        }
        return;
    }

    @Test
    public void test_A014() throws Exception {
        String commandline = "{\n"
                             + "    \"verbose|v\" : \"+\",\n"
                             + "    \"$port|p\" : {\n"
                             + "        \"value\" : 3000,\n"
                             + "        \"type\" : \"int\",\n"
                             + "        \"nargs\" : 1 , \n"
                             + "        \"helpinfo\" : \"port to connect\"\n"
                             + "    },\n"
                             + "    \"dep\" : {\n"
                             + "        \"list|l\" : [],\n"
                             + "        \"string|s\" : \"s_var\",\n"
                             + "        \"$\" : \"+\"\n"
                             + "    }\n"
                             + "}\n";
        Parser parser;
        String depjsonfile = null,jsonfile=null;
        String[] needenvs = {"EXTARGSPARSE_JSON", "DEP_JSON", "EXTARGS_VERBOSE", "EXTARGS_PORT", "DEP_LIST", "DEP_STRING"};
        String[] params = {"-p","9000","dep","--dep-string","ee","ww"};
        NameSpaceEx args;
        this.__unset_environs(needenvs);
        try {
            jsonfile = this.__write_temp_file("parse", ".json", "{\"dep\":{\"list\" : [\"jsonval1\",\"jsonval2\"],\"string\" : \"jsonstring\"},\"port\":6000,\"verbose\":3}\n");
            depjsonfile = this.__write_temp_file("parse",".json","{\"list\":[\"depjson1\",\"depjson2\"]}\n");
            Environ.setenv("EXTARGSPARSE_JSON", jsonfile);
            Environ.setenv("DEP_JSON",depjsonfile);
            parser = new Parser();
            parser.load_command_line_string(commandline);
            args = parser.parse_command_line(params);
            this.assert_long_value(args, "verbose", new Long(3));
            this.assert_long_value(args, "port", new Long(9000));
            this.assert_string_value(args, "subcommand", "dep");
            this.assert_list_value(args, "dep_list", "[\"depjson1\",\"depjson2\"]");
            this.assert_string_value(args, "dep_string", "ee");
            this.assert_list_value(args, "subnargs", "[\"ww\"]");
        } finally {
            if (depjsonfile != null) {
                File file = new File(depjsonfile);
                file.delete();
                depjsonfile = null;
            }
            if (jsonfile != null) {
                File file = new File(jsonfile);
                file.delete();
                jsonfile = null;
            }
        }
        return;
    }

    @Test
    public void test_A015() throws Exception {
    	String commandline="{\n"
            + "    \"verbose|v\" : \"+\",\n"
            + "    \"$port|p\" : {\n"
            + "        \"value\" : 3000,\n"
            + "        \"type\" : \"int\",\n"
            + "        \"nargs\" : 1 , \n"
            + "        \"helpinfo\" : \"port to connect\"\n"
            + "    },\n"
            + "    \"dep\" : {\n"
            + "        \"list|l\" : [],\n"
            + "        \"string|s\" : \"s_var\",\n"
            + "        \"$\" : \"+\"\n"
            + "    }\n"
            + "}";
        Parser parser;
        String depjsonfile = null,jsonfile=null;
        String[] needenvs = {"EXTARGSPARSE_JSON", "DEP_JSON", "EXTARGS_VERBOSE", "EXTARGS_PORT", "DEP_LIST", "DEP_STRING"};
        String[] params = {"-p","9000","--json","jsonfile","dep","--dep-string","ee","ww"};
        NameSpaceEx args;
        int i;
        this.__unset_environs(needenvs);
        try {
            jsonfile = this.__write_temp_file("parse", ".json", "{\"dep\":{\"list\" : [\"jsonval1\",\"jsonval2\"],\"string\" : \"jsonstring\"},\"port\":6000,\"verbose\":3}\n");
            depjsonfile = this.__write_temp_file("parse",".json","{\"list\":[\"depjson1\",\"depjson2\"]}\n");
            Environ.setenv("DEP_JSON",depjsonfile);
            for (i=0;i<params.length;i++) {
            	if (params[i].equals("jsonfile")) {
            		params[i] = jsonfile;
            		break;
            	}
            }
            parser = new Parser();
            parser.load_command_line_string(commandline);
            args = parser.parse_command_line(params);
            this.assert_long_value(args, "verbose", new Long(3));
            this.assert_long_value(args, "port", new Long(9000));
            this.assert_string_value(args, "subcommand", "dep");
            this.assert_list_value(args, "dep_list", "[\"jsonval1\",\"jsonval2\"]");
            this.assert_string_value(args, "dep_string", "ee");
            this.assert_list_value(args, "subnargs", "[\"ww\"]");
        } finally {
            if (depjsonfile != null) {
                File file = new File(depjsonfile);
                file.delete();
                depjsonfile = null;
            }
            if (jsonfile != null) {
                File file = new File(jsonfile);
                file.delete();
                jsonfile = null;
            }
        }
        return;
    }

    @Test
    public void test_A016() throws Exception {
    	String commandline="{\n"
            + "    \"verbose|v\" : \"+\",\n"
            + "    \"$port|p\" : {\n"
            + "        \"value\" : 3000,\n"
            + "        \"type\" : \"int\",\n"
            + "        \"nargs\" : 1 , \n"
            + "        \"helpinfo\" : \"port to connect\"\n"
            + "    },\n"
            + "    \"dep\" : {\n"
            + "        \"list|l\" : [],\n"
            + "        \"string|s\" : \"s_var\",\n"
            + "        \"$\" : \"+\"\n"
            + "    }\n"
            + "}";
        Parser parser;
        String depjsonfile = null,jsonfile=null;
        String[] needenvs = {"EXTARGSPARSE_JSON", "DEP_JSON", "EXTARGS_VERBOSE", "EXTARGS_PORT", "DEP_LIST", "DEP_STRING"};
        String[] params = {"-p","9000","dep","--dep-string","ee","ww"};
        String depstrval,deplistval;
        NameSpaceEx args;
        int i;
        this.__unset_environs(needenvs);
        try {
            depstrval = "newval";
            deplistval = "[\"depenv1\",\"depenv2\"]";
            jsonfile = this.__write_temp_file("parse", ".json", "{\"dep\":{\"list\" : [\"jsonval1\",\"jsonval2\"],\"string\" : \"jsonstring\"},\"port\":6000,\"verbose\":3}\n");
            depjsonfile = this.__write_temp_file("parse",".json","{\"list\":[\"depjson1\",\"depjson2\"]}\n");
            Environ.setenv("DEP_JSON",depjsonfile);
            Environ.setenv("EXTARGSPARSE_JSON",jsonfile);
            Environ.setenv("DEP_STRING",depstrval);
            Environ.setenv("DEP_LIST",deplistval);

            parser = new Parser();
            parser.load_command_line_string(commandline);
            args = parser.parse_command_line(params);
            this.assert_long_value(args, "verbose", new Long(3));
            this.assert_long_value(args, "port", new Long(9000));
            this.assert_string_value(args, "subcommand", "dep");
            this.assert_list_value(args, "dep_list", deplistval);
            this.assert_string_value(args, "dep_string", "ee");
            this.assert_list_value(args, "subnargs", "[\"ww\"]");
        } finally {
            if (depjsonfile != null) {
                File file = new File(depjsonfile);
                file.delete();
                depjsonfile = null;
            }
            if (jsonfile != null) {
                File file = new File(jsonfile);
                file.delete();
                jsonfile = null;
            }
        }
        return;
    }

    @Test
    public void test_A017() throws Exception {
    	String commandline="{\n"
            + "    \"+dpkg\" : {\n"
            + "        \"dpkg\" : \"dpkg\"\n"
            + "    },\n"
            + "    \"verbose|v\" : \"+\",\n"
            + "    \"$port|p\" : {\n"
            + "        \"value\" : 3000,\n"
            + "        \"type\" : \"int\",\n"
            + "        \"nargs\" : 1 , \n"
            + "        \"helpinfo\" : \"port to connect\"\n"
            + "    }\n"
            + "}\n";
        String[] params = {};
        String[] needenvs = {"EXTARGSPARSE_JSON",  "EXTARGS_VERBOSE", "EXTARGS_PORT", "DPKG_DPKG"};
        Parser parser;
        NameSpaceEx args;
        this.__unset_environs(needenvs);
        parser = new Parser();
        parser.load_command_line_string(commandline);
        args = parser.parse_command_line(params);
        this.assert_long_value(args,"verbose",new Long(0));
        this.assert_long_value(args,"port",new Long(3000));
        this.assert_string_value(args,"dpkg_dpkg","dpkg");
        return;
    }

    @Test
    public void test_A018() throws Exception {
    	String commandline="{\n"
            + "    \"+dpkg\" : {\n"
            + "        \"dpkg\" : \"dpkg\"\n"
            + "    },\n"
            + "    \"verbose|v\" : \"+\",\n"
            + "    \"rollback|r\": true,\n"
            + "    \"$port|p\" : {\n"
            + "        \"value\" : 3000,\n"
            + "        \"type\" : \"int\",\n"
            + "        \"nargs\" : 1 , \n"
            + "        \"helpinfo\" : \"port to connect\"\n"
            + "    }\n"
            + "}\n";
        String[] params = {"-vvvv","-r"};
        String[] needenvs = {"EXTARGSPARSE_JSON",  "EXTARGS_VERBOSE", "EXTARGS_ROLLBACK","EXTARGS_PORT", "DPKG_DPKG"};
        Parser parser;
        NameSpaceEx args;
        this.__unset_environs(needenvs);
        parser = new Parser();
        parser.load_command_line_string(commandline);
        args = parser.parse_command_line(params);
        this.assert_long_value(args,"verbose",new Long(4));
        this.assert_bool_value(args,"rollback",new Boolean(false));
        this.assert_long_value(args,"port",new Long(3000));
        this.assert_string_value(args,"dpkg_dpkg","dpkg");
        return;
    }

    @Test
    public void test_A019() throws Exception {
    	String commandline="{\n"
            + "    \"verbose|v\" : \"+\",\n"
            + "    \"$port|p\" : {\n"
            + "        \"value\" : 3000,\n"
            + "        \"type\" : \"int\",\n"
            + "        \"nargs\" : 1 , \n"
            + "        \"helpinfo\" : \"port to connect\"\n"
            + "    },\n"
            + "    \"dep\" : {\n"
            + "        \"list|l\" : [],\n"
            + "        \"string|s\" : \"s_var\",\n"
            + "        \"$\" : \"+\"\n"
            + "    }\n"
            + "}";
        Parser parser;
        String depjsonfile = null,jsonfile=null;
        String[] needenvs = {"EXTARGSPARSE_JSON", "DEP_JSON", "EXTARGS_VERBOSE", "EXTARGS_PORT", "DEP_LIST", "DEP_STRING"};
        String[] params = {"-p","9000","dep","--dep-string","ee","ww"};
        String depstrval,deplistval;
        NameSpaceEx args;
        Priority[] priority= {Priority.ENV_COMMAND_JSON_SET,Priority.ENVIRONMENT_SET,Priority.ENV_SUB_COMMAND_JSON_SET};
        int i;
        this.__unset_environs(needenvs);
        try {
            depstrval = "newval";
            deplistval = "[\"depenv1\",\"depenv2\"]";
            jsonfile = this.__write_temp_file("parse", ".json", "{\"dep\":{\"list\" : [\"jsonval1\",\"jsonval2\"],\"string\" : \"jsonstring\"},\"port\":6000,\"verbose\":3}\n");
            depjsonfile = this.__write_temp_file("parse",".json","{\"list\":[\"depjson1\",\"depjson2\"]}\n");
            Environ.setenv("DEP_JSON",depjsonfile);
            Environ.setenv("EXTARGSPARSE_JSON",jsonfile);
            Environ.setenv("DEP_STRING",depstrval);
            Environ.setenv("DEP_LIST",deplistval);

            parser = new Parser(priority);
            parser.load_command_line_string(commandline);
            args = parser.parse_command_line(params);
            this.assert_long_value(args, "verbose", new Long(3));
            this.assert_long_value(args, "port", new Long(9000));
            this.assert_string_value(args, "subcommand", "dep");
            this.assert_list_value(args, "dep_list", "[\"jsonval1\",\"jsonval2\"]");
            this.assert_string_value(args, "dep_string", "ee");
            this.assert_list_value(args, "subnargs", "[\"ww\"]");
        } finally {
            if (depjsonfile != null) {
                File file = new File(depjsonfile);
                file.delete();
                depjsonfile = null;
            }
            if (jsonfile != null) {
                File file = new File(jsonfile);
                file.delete();
                jsonfile = null;
            }
        }
        return;
    }

    @Test
    public void test_A020() throws Exception {
    	String commandline="{\n"
            + "    \"verbose|v\" : \"+\",\n"
            + "    \"rollback|R\" : true,\n"
            + "    \"$port|P\" : {\n"
            + "        \"value\" : 3000,\n"
            + "        \"type\" : \"int\",\n"
            + "        \"nargs\" : 1 , \n"
            + "        \"helpinfo\" : \"port to connect\"\n"
            + "    },\n"
            + "    \"dep\" : {\n"
            + "        \"list|l\" : [],\n"
            + "        \"string|s\" : \"s_var\",\n"
            + "        \"$\" : \"+\"\n"
            + "    }\n"
            + "}\n";
        String[] params = {"-P","9000","--no-rollback","dep","--dep-string","ee","ww"};
        String[] needenvs = {"EXTARGSPARSE_JSON",  "EXTARGS_VERBOSE", "EXTARGS_ROLLBACK","EXTARGS_PORT","DEP_LIST","DEP_STRING","DEP_JSON"};
        Parser parser;
        NameSpaceEx args;
        this.__unset_environs(needenvs);
        parser = new Parser();
        parser.load_command_line_string(commandline);
        args = parser.parse_command_line(params);
        this.assert_long_value(args,"verbose",new Long(0));
        this.assert_long_value(args,"port",new Long(9000));
        this.assert_bool_value(args,"rollback",new Boolean(false));
        this.assert_string_value(args,"subcommand","dep");
        this.assert_list_value(args,"dep_list","[]");
        this.assert_string_value(args,"dep_string","ee");
        this.assert_list_value(args,"subnargs","[\"ww\"]");
        return;
    }

    @Test
    public void test_A021() throws Exception {
    	String commandline="{\n"
            + "    \"maxval|m\" : 392244922\n"
            + "}";
        String[] params = {"-m","0xffcc"};
        String[] needenvs = {"EXTARGSPARSE_JSON","EXTARGS_MAXVAL"};
        Parser parser;
        NameSpaceEx args;
        this.__unset_environs(needenvs);
        parser = new Parser();
        parser.load_command_line_string(commandline);
        args = parser.parse_command_line(params);
        this.assert_long_value(args,"maxval",new Long(0xffcc));
        return;
    }
}