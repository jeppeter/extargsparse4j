package com.github.jeppeter.extargsparse4j;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.internal.HelpScreenException;

import com.github.jeppeter.extargsparse4j.NameSpaceEx;
import com.github.jeppeter.extargsparse4j.Priority;
import com.github.jeppeter.extargsparse4j.Key;
import com.github.jeppeter.extargsparse4j.Environ;

import com.github.jeppeter.reext.ReExt;
import com.github.jeppeter.jsonext.JsonExt;
import com.github.jeppeter.jsonext.JsonExtInvalidTypeException;
import com.github.jeppeter.jsonext.JsonExtNotParsedException;
import com.github.jeppeter.jsonext.JsonExtNotFoundException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class ParserBase {
    protected Subparser m_parser;
    protected List<Key> m_flags;
    protected String m_cmdname;
    protected Key m_typeclass;
    protected ParserBase(Subparsers parsers, Key keycls) throws NoSuchFieldException, KeyException, IllegalAccessException {
        this.m_parser = parsers.addParser(keycls.get_string_value("cmdname"));
        this.m_flags = new ArrayList<Key>();
        this.m_cmdname = keycls.get_string_value("cmdname");
        this.m_typeclass = keycls;
    }

    @Override
    public String toString() {
        String retstr = "";
        int i;
        Method meth;
        try {
            meth = this.m_parser.getClass().getDeclaredMethod("getCommand");
            retstr += String.format("[parser]=%s(%s);", (String)meth.invoke(this.m_parser), this.m_parser.toString());
        } catch (Exception e) {
            retstr += String.format("[parser]=unknown(%s);", this.m_parser.toString());
        }
        if (false) {
            retstr += String.format("[cmdname]=%s;", this.m_cmdname);
            retstr += String.format("[typeclass]=%s;", this.m_typeclass.toString());
        }
        if (this.m_flags.size() > 0) {
            retstr += "[flags]:";
            for (i = 0; i < this.m_flags.size(); i++) {
                Key curkey;
                curkey = this.m_flags.get(i);
                if (i > 0) {
                    retstr += "|";
                }
                //retstr += String.format("[%d]=%s", i, curkey.toString());
                try {
                    if (curkey.get_string_value("shortopt") != null) {
                        retstr += String.format("[%d]=%s(%s)", i, curkey.get_string_value("longopt"),
                                                curkey.get_string_value("shortopt"));
                    } else {
                        retstr += String.format("[%d]=%s", i, curkey.get_string_value("longopt"));
                    }
                } catch (Exception e) {
                    retstr += String.format("[%d]=unknown", i);
                }
            }
            retstr += ";";
        }
        return retstr;
    }
}

class CountAction implements ArgumentAction {
    @Override
    public void run(ArgumentParser parser, Argument arg,
                    Map<String, Object> attrs, String flag, Object value)
    throws ArgumentParserException {
        Long count = new Long(1);
        if (attrs.containsKey(arg.getDest())) {
            Object obj = attrs.get(arg.getDest());
            if (obj != null) {
                count = (Long) obj;
                count ++;
            }
        }
        if (value != null) {
            if (flag != null && flag.length() == 2 && (value instanceof String)) {
                char shortflag = flag.charAt(1);
                String sobj = (String) value;
                int i;
                for (i = 0; i < sobj.length(); i++) {
                    if (sobj.charAt(i) == shortflag) {
                        count ++;
                    }
                }
            }
        }
        attrs.put(arg.getDest(), count);
    }

    @Override
    public boolean consumeArgument() {
        return true;
    }

    @Override
    public void onAttach(Argument arg) {
    }
}

class LongAction implements ArgumentAction {
    @Override
    public void run(ArgumentParser parser, Argument arg,
                    Map<String, Object> attrs, String flag, Object value)
    throws ArgumentParserException {
        Long count = new  Long(0);
        int radix = 10;
        String sval = (String)value;
        if (sval != null) {
            if (sval.startsWith("0x") ||
                    sval.startsWith("0X") ) {
                sval = sval.substring(2);
                radix = 16;
            } else if ( sval.startsWith("x") ||
                        sval.startsWith("X")) {
            	sval = sval.substring(1);
            	radix = 16;
            }
            count = Long.parseLong(sval, radix);
        }
        attrs.put(arg.getDest(), count);
    }

    @Override
    public boolean consumeArgument() {
        return true;
    }

    @Override
    public void onAttach(Argument arg) {
    }
}

class DoubleAction implements ArgumentAction {
    @Override
    public void run(ArgumentParser parser, Argument arg,
                    Map<String, Object> attrs, String flag, Object value)
    throws ArgumentParserException {
        Double count = Double.parseDouble((String)value);
        attrs.put(arg.getDest(), count);
    }

    @Override
    public boolean consumeArgument() {
        return true;
    }

    @Override
    public void onAttach(Argument arg) {
    }
}

class FalseAction implements ArgumentAction {
    @Override
    public void run(ArgumentParser parser, Argument arg,
                    Map<String, Object> attrs, String flag, Object value)
    throws ArgumentParserException {
        Boolean bobj = false;
        attrs.put(arg.getDest(), bobj);
    }

    @Override
    public boolean consumeArgument() {
        return false;
    }

    @Override
    public void onAttach(Argument arg) {
    }
}

class TrueAction implements ArgumentAction {
    @Override
    public void run(ArgumentParser parser, Argument arg,
                    Map<String, Object> attrs, String flag, Object value)
    throws ArgumentParserException {
        Boolean bobj = true;
        attrs.put(arg.getDest(), bobj);
    }

    @Override
    public boolean consumeArgument() {
        return false;
    }

    @Override
    public void onAttach(Argument arg) {
    }
}

class ListAction implements ArgumentAction {
    @Override
    public void run(ArgumentParser parser, Argument arg,
                    Map<String, Object> attrs, String flag, Object value)
    throws ArgumentParserException {
        List<String> lobj;
        Object obj;
        obj = attrs.get(arg.getDest());
        if (obj == null) {
            lobj = new ArrayList<String>();
        } else {
            lobj = (List<String>) obj;
        }
        lobj.add((String)value);
        attrs.put(arg.getDest(), lobj);
    }

    @Override
    public boolean consumeArgument() {
        return true;
    }

    @Override
    public void onAttach(Argument arg) {
    }
}

public class Parser  {
    private Logger m_logger;
    private Priority[] m_priorities;
    private List<Key> m_flags;
    private Subparsers m_subparsers;
    private ArgumentParser m_parser;
    private HashMap<String, Method> m_functable;
    private List<ParserBase> m_cmdparsers;
    private HashMap<Priority, Method> m_argsettable;

    private NameSpaceEx call_func_args(String funcname, NameSpaceEx args, Object ctx) throws ClassNotFoundException, ParserException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method meth = null;
        ClassLoader clsloader = this.getClass().getClassLoader();
        String[] clss = ReExt.Split("\\.", funcname);
        int i;
        String clsname;
        String methname;
        Class ldcls;
        StackTraceElement[] stacks;
        Class[] clsobj = new Class[2];

        if (clss.length > 1) {
            clsname = "";
            for (i = 0; i < (clss.length - 1); i ++) {
                if (i > 0) {
                    clsname += ".";
                }
                clsname += clss[i];
            }

            /**/
            ldcls = clsloader.loadClass(clsname);
            methname = clss[clss.length - 1];
        } else {
            stacks = Thread.currentThread().getStackTrace();
            if (stacks.length < 4) {
                throw new ParserException(String.format("get stack stack 3"));
            }
            clsname = stacks[3].getClassName();

            ldcls = clsloader.loadClass(clsname);
            methname = clss[0];
        }
        this.m_logger.info(String.format("clsname %s methname %s", clsname, methname));
        /*now we should get the function*/
        clsobj[0] = NameSpaceEx.class;
        clsobj[1] = Object.class;
        meth = ldcls.getDeclaredMethod(methname, clsobj);

        return (NameSpaceEx)meth.invoke(null, args, ctx);
    }

    private static String get_main_class() {
        String command = System.getProperty("sun.java.command");
        String[] names;
        String mainclass;
        names = ReExt.Split("\\s+", command);
        mainclass = "Parser";
        if (names.length > 0) {
            if (names[0].contains(".")) {
                names = ReExt.Split("\\.", names[0]);
                /*get last one*/
                mainclass = names[(names.length - 1)];
            } else {
                mainclass = names[0];
            }
        }
        return mainclass;
    }

    private Boolean __check_flag_insert(Key keycls, ParserBase curparser) throws NoSuchFieldException, KeyException, IllegalAccessException {
        Boolean valid = false;
        int i;
        Key curcls;
        if (curparser != null) {
            valid = true;
            for (i = 0; i < curparser.m_flags.size(); i ++) {
                curcls = curparser.m_flags.get(i);
                if ( curcls.get_bool_value("isflag") &&
                        !curcls.get_string_value("flagname").equals("$") &&
                        !keycls.get_string_value("flagname").equals("$")) {
                    if (curcls.get_string_value("optdest").equals(keycls.get_string_value("optdest"))) {
                        valid = false;
                        break;
                    }
                }  else if (curcls.get_string_value("flagname") ==
                            keycls.get_string_value("flagname")) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                curparser.m_flags.add(keycls);
            }
        } else {
            valid = true;
            for (i = 0; i < this.m_flags.size(); i++) {
                curcls = this.m_flags.get(i);
                if ( curcls.get_bool_value("isflag") &&
                        !curcls.get_string_value("flagname").equals("$") &&
                        !keycls.get_string_value("flagname").equals("$")) {
                    if (curcls.get_string_value("optdest").equals(keycls.get_string_value("optdest"))) {
                        valid = false;
                        break;
                    }
                } else if ( curcls.get_bool_value("isflag") &&
                            curcls.get_string_value("flagname").equals(keycls.get_string_value("flagname"))) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                this.m_flags.add(keycls);
            }
        }
        return valid;
    }

    private Boolean __check_flag_insert_mustsucc(Key keycls, ParserBase curparser) throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException {
        Boolean valid;
        valid = this.__check_flag_insert(keycls, curparser);
        if (! valid ) {
            String cmdname;
            cmdname = "main";
            if (curparser != null) {
                cmdname = curparser.m_cmdname;
            }
            throw new ParserException(String.format("(%s) already in (%s)", keycls.get_string_value("flagname"), cmdname));
        }
        return valid;
    }

    private String __get_help_info(Key keycls) throws NoSuchFieldException, KeyException, IllegalAccessException {
        String helpinfo = "", s;
        String typestr;
        Boolean bobj;
        String sobj;
        Object obj;
        typestr = keycls.get_string_value("type");

        if (typestr.equals("bool")) {
            bobj = (Boolean)keycls.get_object_value("value");
            if (bobj) {
                helpinfo += String.format("%s set false default(True)", keycls.get_string_value("optdest"));
            } else {
                helpinfo += String.format("%s set true default(False)", keycls.get_string_value("optdest"));
            }
        } else if (typestr.equals("string")) {
            sobj = (String) keycls.get_object_value("value");
            if (sobj != null) {
                helpinfo += String.format("%s set default(%s)", keycls.get_string_value("optdest"), sobj);
            } else {
                helpinfo += String.format("%s set default(null)", keycls.get_string_value("optdest"));
            }
        } else {
            if (keycls.get_bool_value("isflag")) {
                obj = keycls.get_object_value("value");
                assert(obj != null);
                helpinfo += String.format("%s set default(%s)", keycls.get_string_value("optdest"), obj.toString());
            } else {
                assert(keycls.get_bool_value("iscmd"));
                helpinfo += String.format("%s command exec", keycls.get_string_value("cmdname"));
            }
        }

        if (keycls.get_string_value("helpinfo") != null)  {
            helpinfo = keycls.get_string_value("helpinfo");
        }

        return helpinfo;
    }

    private Boolean __load_command_line_inner_action(String prefix, Key keycls, ParserBase curparser, ArgumentAction act)  throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException {
        String longopt, shortopt, optdest, helpinfo;
        Argument thisarg;
        Boolean valid;
        valid = this.__check_flag_insert(keycls, curparser);
        if (!valid) {
            return false;
        }
        longopt = keycls.get_string_value("longopt");
        shortopt = keycls.get_string_value("shortopt");
        optdest = keycls.get_string_value("optdest");
        helpinfo = this.__get_help_info(keycls);

        if (curparser != null) {
            if (shortopt != null) {
                curparser.m_parser.addArgument(shortopt, longopt).dest(optdest).setDefault((Object)null).action(act).help(helpinfo);
            } else {
                curparser.m_parser.addArgument(longopt).dest(optdest).setDefault((Object)null).action(act).help(helpinfo);
            }
            //this.m_logger.info(String.format("%s shortopt %s longopt %s action(%s) helpinfo(%s)", curparser.toString(), shortopt, longopt, act.toString(), helpinfo));
        } else {
            if (shortopt != null) {
                this.m_parser.addArgument(shortopt, longopt).dest(optdest).setDefault((Object)null).action(act).help(helpinfo);
            } else {
                this.m_parser.addArgument(longopt).dest(optdest).setDefault((Object)null).action(act).help(helpinfo);
            }
            //this.m_logger.info(String.format("null shortopt %s longopt %s action(%s) helpinfo(%s)", shortopt, longopt, act.toString(), helpinfo));
        }
        return true;
    }

    private Boolean __load_command_line_string(String prefix, Key keycls, ParserBase curparser) throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException {
        return this.__load_command_line_inner_action(prefix, keycls, curparser, Arguments.store());
    }

    private Boolean __load_command_line_count(String prefix, Key keycls, ParserBase curparser) throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException {
        return this.__load_command_line_inner_action(prefix, keycls, curparser, new CountAction());
    }

    private Boolean __load_command_line_int(String prefix, Key keycls, ParserBase curparser) throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException {
        return this.__load_command_line_inner_action(prefix, keycls, curparser, new LongAction());
    }

    private Boolean __load_command_line_float(String prefix, Key keycls, ParserBase curparser)  throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException {
        return this.__load_command_line_inner_action(prefix, keycls, curparser, new DoubleAction());
    }

    private Boolean __load_command_line_list(String prefix, Key keycls, ParserBase curparser) throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException {
        return this.__load_command_line_inner_action(prefix, keycls, curparser, Arguments.append());
    }

    private Boolean __load_command_line_bool(String prefix, Key keycls, ParserBase curparser) throws NoSuchFieldException, ParserException, KeyException, IllegalAccessException {
        Boolean bobj = (Boolean) keycls.get_object_value("value");
        if (!bobj) {
            return this.__load_command_line_inner_action(prefix, keycls, curparser, new TrueAction());
        }
        return this.__load_command_line_inner_action(prefix, keycls, curparser, new FalseAction());
    }

    private Boolean __load_command_line_args(String prefix, Key keycls, ParserBase curparser) throws NoSuchFieldException, ParserException, KeyException, IllegalAccessException {
        Boolean valid;
        String optdest = "args", helpinfo, nargs;
        Argument arg;
        valid = this.__check_flag_insert(keycls, curparser);
        if (! valid) {
            return false;
        }

        if (curparser != null) {
            optdest = "subnargs";
        }

        helpinfo = keycls.get_string_value("helpinfo");
        if (helpinfo == null) {
            helpinfo = String.format("%s set ", optdest);
        }
        nargs = keycls.get_string_value("nargs");

        if ( ! nargs.equals("0")) {
            if (curparser != null) {
                arg = curparser.m_parser.addArgument(optdest);
            } else {
                arg = this.m_parser.addArgument(optdest);
            }

            arg.metavar(optdest);
            if (nargs.equals("+") || nargs.equals("*") || nargs.equals("?")) {
                arg.nargs(nargs);
            } else {
                arg.nargs(Integer.parseInt(nargs));
            }
            arg.action(Arguments.store());
            arg.help(helpinfo);
        }
        return true;
    }

    private Boolean __load_command_prefix(String prefix, Key keycls, ParserBase curparser) throws NoSuchFieldException, ParserException, KeyException, IllegalAccessException, JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException, InvocationTargetException {
        this.__load_command_line_inner(keycls.get_string_value("prefix"), keycls.get_object_value("value"), curparser);
        return true;
    }

    private Boolean __load_command_line_jsonfile(String prefix, Key keycls , ParserBase curparser) throws NoSuchFieldException, ParserException, KeyException, IllegalAccessException {
        return this.__load_command_line_inner_action(prefix, keycls, curparser, Arguments.store());
    }

    private Boolean __load_command_line_json_added(ParserBase curparser) throws NoSuchFieldException, ParserException, KeyException, IllegalAccessException, JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException {
        String prefix = "";
        String key = "json## json input file to get the value set ##";
        Object value = null;
        Key keycls;
        if (curparser != null) {
            prefix = curparser.m_cmdname;
        }
        keycls = new Key(prefix, key, value, true);
        return this.__load_command_line_jsonfile(prefix, keycls, curparser);
    }

    private ParserBase __find_subparser_inner(String cmdname) {
        int i;
        ParserBase parsebase;
        for (i = 0; i < this.m_cmdparsers.size(); i++) {
            parsebase = this.m_cmdparsers.get(i);
            if (parsebase.m_cmdname.equals(cmdname) ) {
                return parsebase;
            }
        }
        return null;
    }

    private ParserBase __get_subparser_inner(Key keycls) throws NoSuchFieldException, KeyException, IllegalAccessException {
        ParserBase cmdparser = null;
        String helpinfo;
        ArgumentParser parser = null;
        cmdparser = this.__find_subparser_inner(keycls.get_string_value("cmdname"));
        if (cmdparser != null) {
            return cmdparser;
        }
        if (this.m_subparsers == null) {
            this.m_subparsers = this.m_parser.addSubparsers().dest("subcommand").help("");
            //this.m_logger.info(String.format("create subparsers (%s) parser(%s)", this.m_subparsers.toString(), this.m_parser.toString()));
        }
        helpinfo = this.__get_help_info(keycls);
        cmdparser = new ParserBase(this.m_subparsers, keycls);
        cmdparser.m_parser.help(helpinfo);
        //this.m_logger.info(String.format("new (%s) keycls(%s) subparsers(%s)", cmdparser.toString(), keycls.toString(), this.m_subparsers.toString()));
        this.m_cmdparsers.add(cmdparser);
        return cmdparser;
    }

    private Boolean __load_command_subparser(String prefix, Key keycls, ParserBase curparser) throws NoSuchFieldException, KeyException, IllegalAccessException, ParserException, JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException, InvocationTargetException {
        Object vobj;
        ParserBase nextparser = null;
        if (curparser != null) {
            throw new ParserException(String.format("(%s) can not make command recursively", keycls.get_string_value("origkey")));
        }

        vobj = keycls.get_object_value("value");
        if (!(vobj instanceof JSONObject)) {
            throw new ParserException(String.format("(%s) must be value dict", keycls.get_string_value("origkey")));
        }

        nextparser = this.__get_subparser_inner(keycls);
        this.__load_command_line_inner(keycls.get_string_value("prefix"), (JSONObject)vobj, nextparser);
        return true;
    }

    public Parser(Priority[] priority, String caption, String description, Boolean defaulthelp) throws Exception {


        try {
            Priority[] defpriority = {Priority.SUB_COMMAND_JSON_SET ,
                                      Priority.COMMAND_JSON_SET , Priority.ENVIRONMENT_SET,
                                      Priority.ENV_SUB_COMMAND_JSON_SET , Priority.ENV_COMMAND_JSON_SET
                                     };
            Class[] cls = new Class[3];
            Class[] argcls = new Class[1];

            this.m_logger = LogManager.getLogger(this.getClass().getName());
            if (priority.length == 0 ) {
                this.m_priorities = defpriority;
            } else {
                this.m_priorities = priority;
            }

            this.m_flags = new ArrayList<Key>();
            this.m_subparsers = null;

            //this.m_logger.info(String.format("priority (%s) m_priorities (%s) caption(%s) description (%s) help %s",
            //                                 priority.toString(), this.m_priorities.toString(), caption, description, defaulthelp ? "True" : "False"));
            this.m_parser = ArgumentParsers.newArgumentParser(caption)
                            .defaultHelp(defaulthelp)
                            .description(description);
            this.m_functable = new HashMap<String, Method>();
            cls[0] = String.class;
            cls[1] = Key.class;
            cls[2] = ParserBase.class;
            this.m_functable.put("string", this.getClass().getDeclaredMethod("__load_command_line_string", cls));
            this.m_functable.put("long", this.getClass().getDeclaredMethod("__load_command_line_int", cls));
            this.m_functable.put("float", this.getClass().getDeclaredMethod("__load_command_line_float", cls));
            this.m_functable.put("list", this.getClass().getDeclaredMethod("__load_command_line_list", cls));
            this.m_functable.put("bool", this.getClass().getDeclaredMethod("__load_command_line_bool", cls));
            this.m_functable.put("args", this.getClass().getDeclaredMethod("__load_command_line_args", cls));
            this.m_functable.put("count", this.getClass().getDeclaredMethod("__load_command_line_count", cls));
            this.m_functable.put("command", this.getClass().getDeclaredMethod("__load_command_subparser", cls));
            this.m_functable.put("prefix", this.getClass().getDeclaredMethod("__load_command_prefix", cls));

            argcls[0] = NameSpaceEx.class;
            this.m_argsettable = new HashMap<Priority, Method>();
            this.m_argsettable.put(Priority.SUB_COMMAND_JSON_SET, this.getClass().getDeclaredMethod("__parse_sub_command_json_set", argcls));
            this.m_argsettable.put(Priority.COMMAND_JSON_SET, this.getClass().getDeclaredMethod("__parse_command_json_set", argcls));
            this.m_argsettable.put(Priority.ENVIRONMENT_SET, this.getClass().getDeclaredMethod("__parse_environment_set", argcls));
            this.m_argsettable.put(Priority.ENV_SUB_COMMAND_JSON_SET, this.getClass().getDeclaredMethod("__parse_env_subcommand_json_set", argcls));
            this.m_argsettable.put(Priority.ENV_COMMAND_JSON_SET, this.getClass().getDeclaredMethod("__parse_env_command_json_set", argcls));
            this.m_cmdparsers = new ArrayList<ParserBase>();
        } catch (Exception e) {
            throw (Exception)e;
            //throw new ParserException(String.format("%s:%s",e.getClass().getName(),e.toString()));
        }

    }

    public Parser(Priority[] priority, String caption, String description) throws Exception {
        this(priority, caption, description, true);
    }

    public Parser(Priority[] priority, String caption) throws Exception {
        this(priority, caption, String.format("%s [OPTIONS] command ...", caption));
    }

    public Parser(Priority[] priority) throws Exception {
        this(priority, "", get_main_class());
    }

    public Parser() throws Exception {
        this(new Priority[] {});
    }

    private NameSpaceEx __set_jsonvalue_not_defined_inner(NameSpaceEx args, List<Key> flagarray, String key, Object value) throws NoSuchFieldException, KeyException, IllegalAccessException {
        int i;
        Key p;
        for (i = 0; i < flagarray.size(); i++) {
            p = flagarray.get(i);
            if (p.get_bool_value("isflag") &&
                    !p.get_string_value("type").equals("prefix") &&
                    !p.get_string_value("type").equals("args") ) {
                if (p.get_string_value("optdest").equals(key)) {
                    if (args.get(key) == null) {
                        String vtypestr;
                        String ptypestr;
                        TypeClass typecls;
                        typecls = new TypeClass(value);
                        vtypestr = typecls.get_type();
                        typecls = new TypeClass(p.get_object_value("value"));
                        ptypestr = typecls.get_type();
                        if (vtypestr.equals(ptypestr)) {
                            args.set(key, value);
                        }
                    }
                    return args;
                }
            }
        }
        return null;
    }

    private NameSpaceEx __set_jsonvalue_not_defined(NameSpaceEx args, List<Key> flagarray, String key, Object value) throws NoSuchFieldException, KeyException, IllegalAccessException {
        NameSpaceEx oldargs = args;
        NameSpaceEx retargs;
        ParserBase curparser;
        int i;
        retargs = this.__set_jsonvalue_not_defined_inner(args, flagarray, key, value);
        if (retargs != null) {
            return retargs;
        }

        retargs = this.__set_jsonvalue_not_defined_inner(args, this.m_flags, key, value);
        if (retargs != null) {
            return retargs;
        }

        for (i = 0; i < this.m_cmdparsers.size(); i++) {
            curparser = this.m_cmdparsers.get(i);
            retargs = this.__set_jsonvalue_not_defined_inner(args, curparser.m_flags, key, value);
            if (retargs != null) {
                return retargs;
            }
        }
        return oldargs;
    }

    private NameSpaceEx __load_jsonvalue(NameSpaceEx args, String prefix, Object jsonvalue, List<Key> flagarray) throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException {
        JSONObject jobj = null;
        int i;
        Object val;
        Set<String> keyset;
        String[] keys;
        String curprefix = "";
        if (! (jsonvalue instanceof JSONObject)) {
            throw new ParserException(String.format("value type (%s) not JSONObject", jsonvalue.getClass().getName()));
        }

        jobj = (JSONObject) jsonvalue;
        keyset = jobj.keySet();
        keys = keyset.toArray(new String[keyset.size()]);
        for (i = 0; i < keys.length; i++) {
            val = jobj.get(keys[i]);
            if (val instanceof JSONObject) {
                curprefix = "";
                if (prefix.length() > 0) {
                    curprefix += String.format("%s_", prefix);
                }
                curprefix += keys[i];
                args = this.__load_jsonvalue(args, curprefix, val, flagarray);
            } else {
                curprefix = "";
                if (prefix.length() > 0) {
                    curprefix += String.format("%s_", prefix);
                }
                curprefix += keys[i];
                args = this.__set_jsonvalue_not_defined(args, flagarray, curprefix, val);
            }
        }
        return args;
    }

    private NameSpaceEx __load_jsonfile(NameSpaceEx args, String subcmd, String jsonfile, ParserBase curparser) throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException, JsonExtNotParsedException, JsonExtNotFoundException, JsonExtInvalidTypeException {
        String prefix = "";
        List<Key> flagarray = null;
        JsonExt jext;
        Object jsonvalue = null;
        assert(jsonfile != null);
        if (subcmd != null) {
            prefix += subcmd;
        }

        flagarray = this.m_flags;
        if (curparser != null) {
            flagarray = curparser.m_flags;
        }

        /*now we read file and give the jobs*/
        jext = new JsonExt();
        jext.parseFile(jsonfile);
        jsonvalue = jext.getObject("/");
        return this.__load_jsonvalue(args, prefix, jsonvalue, flagarray);
    }

    private NameSpaceEx __parse_sub_command_json_set(NameSpaceEx args) throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException, JsonExtNotParsedException, JsonExtNotFoundException, JsonExtInvalidTypeException {
        if (this.m_subparsers != null && args.getString("subcommand") != null) {
            String jsondest = String.format("%s_json", args.getString("subcommand"));
            ParserBase curparser = this.__find_subparser_inner(args.getString("subcommand"));
            String jsonfile;
            assert(curparser != null);
            jsonfile = args.getString(jsondest);
            if (jsonfile != null) {
                this.m_logger.info(String.format("jsonfile %s", jsonfile));
                args = this.__load_jsonfile(args, args.getString("subcommand"), jsonfile, curparser);
            }
        }
        return args;
    }

    private NameSpaceEx __parse_command_json_set(NameSpaceEx args) throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException, JsonExtNotParsedException, JsonExtNotFoundException, JsonExtInvalidTypeException {
        if (args.getString("json") != null) {
            String jsonfile = args.getString("json");
            if (jsonfile != null) {
                args = this.__load_jsonfile(args, "", jsonfile, null );
            }
        }
        return args;
    }


    private NameSpaceEx __parse_env_subcommand_json_set(NameSpaceEx args) throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException, JsonExtNotParsedException, JsonExtNotFoundException, JsonExtInvalidTypeException {
        if (this.m_subparsers != null && args.getString("subcommand") != null) {
            String jsondest = String.format("%s_json", args.getString("subcommand"));
            ParserBase curparser = this.__find_subparser_inner(args.getString("subcommand"));
            String jsonfile;
            assert(curparser != null);
            jsondest = jsondest.replace('-', '_');
            jsondest = jsondest.toUpperCase();
            jsonfile = Environ.getenv(jsondest);
            if (jsonfile != null && jsonfile.length() > 0) {
                args = this.__load_jsonfile(args, args.getString("subcommand"), jsonfile, curparser);
            }
        }
        return args;
    }

    private NameSpaceEx __parse_env_command_json_set(NameSpaceEx args) throws ParserException, NoSuchFieldException, KeyException, IllegalAccessException, JsonExtNotParsedException, JsonExtNotFoundException, JsonExtInvalidTypeException {
        String jsonfile;
        jsonfile = Environ.getenv("EXTARGSPARSE_JSON");
        if (jsonfile != null && jsonfile.length() > 0) {
            args = this.__load_jsonfile(args, "", jsonfile, null);
        }
        return args;
    }

    private NameSpaceEx __set_environ_value_inner(NameSpaceEx args, String prefix, List<Key> flagarray ) throws NoSuchFieldException, KeyException, IllegalAccessException, JsonExtNotParsedException, JsonExtNotFoundException, JsonExtInvalidTypeException, ParserException {
        int i;
        Key keycls;
        for (i = 0; i < flagarray.size(); i++) {
            keycls = flagarray.get(i);
            if (keycls.get_bool_value("isflag") &&
                    !keycls.get_string_value("type").equals("prefix") &&
                    !keycls.get_string_value("type").equals("args")) {
                String optdest;
                String oldopt;
                String val;
                optdest = keycls.get_string_value("optdest");
                oldopt = optdest;
                if (args.get(oldopt) != null) {
                    continue;
                }
                if (optdest.indexOf('_') < 0) {
                    optdest = String.format("EXTARGS_%s", optdest);
                }
                val = Environ.getenv(optdest);
                if (val != null && val.length() > 0) {
                    if (keycls.get_string_value("type") == "string") {
                        args.set(oldopt, (Object)val);
                    } else if (keycls.get_string_value("type") == "bool") {
                        Boolean bval;
                        if (val.toLowerCase() == "true") {
                            bval = true;
                            args.set(oldopt, bval);
                        } else if (val.toLowerCase() == "false") {
                            bval = false;
                            args.set(oldopt, bval);
                        }
                    } else if (keycls.get_string_value("type") == "list") {
                        String jsonstr = String.format("{ \"dummy\" : %s }", (String)val);
                        JsonExt jsonext = new JsonExt();
                        Object jobj;
                        Object obj;
                        JSONArray jarr;
                        List<String> lobj;
                        int jidx;
                        jsonext.parseString(jsonstr);
                        jobj = jsonext.getObject("/dummy");
                        this.m_logger.info(String.format("jobj (%s)", jobj.toString()));
                        if ( jobj instanceof JSONArray) {
                            lobj = new ArrayList<String>();
                            jarr = (JSONArray) jobj;
                            for (jidx = 0; jidx < jarr.size(); jidx ++) {
                                obj = jarr.get(jidx);
                                if (!(obj instanceof String)) {
                                    throw new ParserException(String.format("%s(%s)[%d] not string object", optdest, (String)val, i));
                                }
                                lobj.add((String)obj);
                            }
                        } else {
                            throw new ParserException(String.format("%s(%s) not valid list", optdest, (String)val));
                        }
                        args.set(oldopt, lobj);
                    } else if (keycls.get_string_value("type") == "int" ) {
                        args.set(oldopt, Integer.parseInt((String)val));
                    } else if (keycls.get_string_value("type") == "float") {
                        args.set(oldopt, Float.parseFloat((String)val));
                    } else {
                        throw new ParserException(String.format("unknown type(%s) for (%s)", keycls.get_string_value("type"), oldopt));
                    }
                }
            }
        }

        return args;
    }

    private NameSpaceEx __set_environ_value(NameSpaceEx args)  throws NoSuchFieldException, KeyException, IllegalAccessException, JsonExtNotParsedException, JsonExtNotFoundException, JsonExtInvalidTypeException, ParserException {
        int i;
        ParserBase curparser;
        for (i = 0; i < this.m_cmdparsers.size(); i++) {
            curparser = this.m_cmdparsers.get(i);
            args = this.__set_environ_value_inner(args, curparser.m_cmdname, curparser.m_flags);
        }
        return this.__set_environ_value_inner(args, "", this.m_flags);
    }

    private NameSpaceEx __parse_environment_set(NameSpaceEx args) throws NoSuchFieldException, KeyException, IllegalAccessException, JsonExtNotParsedException, JsonExtNotFoundException, JsonExtInvalidTypeException, ParserException {
        return this.__set_environ_value(args);
    }


    private void __load_command_line_inner(String prefix, Object obj, ParserBase curparser) throws NoSuchFieldException, KeyException, ParserException, JsonExtInvalidTypeException, IllegalAccessException, JsonExtNotParsedException, JsonExtNotFoundException, InvocationTargetException {
        JSONObject jobj;
        Object val;
        String[] keys;
        Method meth;
        String type;
        Key keycls;
        int i;
        Set<String> keyset;
        Boolean valid;
        this.__load_command_line_json_added(curparser);
        jobj = (JSONObject) obj;
        keyset = jobj.keySet();
        keys = keyset.toArray(new String[keyset.size()]);
        for (i = 0; i < keys.length; i++) {
            val = jobj.get(keys[i]);
            if (curparser != null) {
                //this.m_logger.info(String.format("(%s) , (%s) , (%s):(%s) , True", prefix, keys[i], val.toString(), val.getClass().getName()));
                keycls = new Key(prefix, keys[i], val, true);
            } else {
                //this.m_logger.info(String.format("(%s) , (%s) , (%s):(%s) , False", prefix, keys[i], val.toString(), val.getClass().getName()));
                keycls = new Key(prefix, keys[i], val, false);
            }

            //this.m_logger.info(String.format("keycls %s ", keycls.toString()));
            meth = this.m_functable.get(keycls.get_string_value("type"));
            assert(meth != null);
            //this.m_logger.info(String.format("metho %s", meth.toString()));
            valid = (Boolean)meth.invoke(this, (Object)prefix, (Object)keycls, (Object)curparser);
            if (! valid) {
                throw new ParserException(String.format("can not add %s %s", keys[i], val.toString()));
            }
        }
        return;
    }

    public void load_command_line(Object obj) throws Exception {
        if (!(obj instanceof JSONObject)) {
            throw new ParserException(String.format("obj is not JSONObject"));
        }
        try {
            this.__load_command_line_inner("", obj, null);
        } catch (Exception e) {
            throw (Exception) e;
            //throw new ParserException(String.format("%s:%s",e.getClass().getName(),e.toString()));
        }
        return;
    }

    public void load_command_line_string(String str) throws Exception {

        Object obj;
        JsonExt jext;
        try {
            jext = new JsonExt();
            jext.parseString(str);
            obj = jext.getObject("/");
        } catch (Exception e) {
            throw new ParserException(String.format("%s:%s", e.getClass().getName(), e.toString()));
        }
        this.load_command_line(obj);
        return;
    }

    private void __set_command_line_self_args() throws KeyException, JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException, NoSuchFieldException, IllegalAccessException, ParserException {
        Key keycls;
        ParserBase curparser;
        int i;
        /*we put the key args for things "*" */
        for (i = 0; i < this.m_cmdparsers.size(); i++) {
            curparser = this.m_cmdparsers.get(i);
            keycls = new Key(curparser.m_cmdname, "$", "*", true);
            this.__load_command_line_args(curparser.m_cmdname, keycls, curparser);
            keycls = null;
        }

        if (this.m_cmdparsers.size() == 0) {
            keycls = new Key("", "$", "*", true);
            this.__load_command_line_args("", keycls, null);
        }
        keycls = null;
        return ;
    }

    private NameSpaceEx __set_default_value(NameSpaceEx args) throws NoSuchFieldException, KeyException, IllegalAccessException {
        /*first to set the value for the flagarray*/
        List<Key> flagarray;
        int i, j;
        Key curkey;
        flagarray = this.m_flags;
        for (i = 0; i < flagarray.size(); i++) {
            curkey = flagarray.get(i);
            if (curkey.get_bool_value("isflag") &&
                    !curkey.get_string_value("flagname").equals("$")) {
                args = this.__set_jsonvalue_not_defined(args, flagarray,
                                                        curkey.get_string_value("optdest"),
                                                        curkey.get_object_value("value"));
            }
        }
        for (i = 0; i < this.m_cmdparsers.size(); i++) {
            flagarray = this.m_cmdparsers.get(i).m_flags;
            for (j = 0; j < flagarray.size(); j++) {
                curkey = flagarray.get(j);
                if (curkey.get_bool_value("isflag") &&
                        !curkey.get_string_value("flagname").equals("$")) {
                    args = this.__set_jsonvalue_not_defined(args, flagarray,
                                                            curkey.get_string_value("optdest"),
                                                            curkey.get_object_value("value"));
                }
            }
        }
        return args;
    }

    private NameSpaceEx __check_args_number(NameSpaceEx args, String argsname, String argsnum) throws ParserException {
        List<String> argscont;
        int inum;
        Boolean valid = true;
        argscont = args.getList(argsname);
        if (argsnum.equals("+")) {
            if (argscont.size() < 1) {
                valid = false;
            }
        } else if (argsnum.equals("?")) {
            if (argscont.size() > 1) {
                valid = false;
            }
        } else if (argsnum.equals("*")) {

        } else {
            inum = Integer.parseInt(argsnum);
            if (argscont.size() != inum) {
                valid = false;
            }
        }
        if (!valid) {
            throw new ParserException(String.format("count %d for %s", argscont.size(), argsnum));
        }

        return args;
    }


    private NameSpaceEx __check_args(NameSpaceEx args) throws NoSuchFieldException, KeyException, IllegalAccessException, ParserException {
        int i;
        int j = 0;
        List<Key> flagarray;
        Key curkey;
        String nargs;

        if (this.m_cmdparsers.size() > 0) {
            flagarray = null;
            for (i = 0; i < this.m_cmdparsers.size(); i++) {
                if (this.m_cmdparsers.get(i).m_cmdname.equals(args.getString("subcommand"))) {
                    flagarray = this.m_cmdparsers.get(i).m_flags;
                }
            }
            assert(flagarray != null);
            //this.m_logger.info(String.format("subnargs (%s)", args.getString("subnargs")));

            for (i = 0; i < flagarray.size(); i++) {
                curkey = flagarray.get(i);
                if (curkey.get_bool_value("isflag") &&
                        curkey.get_string_value("flagname").equals("$")) {
                    assert(j == 0);
                    nargs = curkey.get_string_value("nargs");
                    args = this.__check_args_number(args, "subnargs", nargs);
                    j = 1;
                }
            }
        } else {
            flagarray = this.m_flags;
            //this.m_logger.info(String.format("args (%s)", args.getString("args")));
            for (i = 0; i < flagarray.size(); i++) {
                curkey = flagarray.get(i);
                if (curkey.get_bool_value("isflag") &&
                        curkey.get_string_value("flagname").equals("$")) {
                    assert(j == 0);
                    nargs = curkey.get_string_value("nargs");
                    args = this.__check_args_number(args, "args", nargs);
                    j = 1;
                }
            }
        }
        assert(j == 1);
        return args;
    }

    private NameSpaceEx __parse_command_line_inner(String[] params, Object ctx) throws KeyException, JsonExtInvalidTypeException, JsonExtNotParsedException, JsonExtNotFoundException, NoSuchFieldException, IllegalAccessException, ParserException, ArgumentParserException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException {
        NameSpaceEx args = null;
        Namespace ns;
        Priority curprio;
        int i;
        Method meth;
        ParserBase curparser;
        String funcname;
        this.__set_command_line_self_args();
        try {
            ns = this.m_parser.parseArgs(params);
            args = new NameSpaceEx(ns);
            for (i = 0; i < this.m_priorities.length; i++) {
                curprio = this.m_priorities[i];
                meth = this.m_argsettable.get(curprio);
                //this.m_logger.info(String.format("prior %s meth %s", curprio.toString(), meth.toString()));
                args = (NameSpaceEx) meth.invoke(this, (Object)args);
                //this.m_logger.info(String.format("args (%s)",args.toString()));
            }
            args = this.__set_default_value(args);
            args = this.__check_args(args);

            if (this.m_subparsers != null && args.getString("subcommand") != null) {
                curparser = this.__find_subparser_inner(args.getString("subcommand"));
                assert(curparser != null);
                funcname = curparser.m_typeclass.get_string_value("function");
                if (funcname != null && funcname.length() > 0) {
                    this.call_func_args(funcname, args, ctx);
                }
            }
            return args;
        } catch (HelpScreenException e) {
            System.exit(0);
        }
        return args;
    }

    public NameSpaceEx parse_command_line(String params[], Object ctx) throws Exception {
        NameSpaceEx args;
        try {
            args = this.__parse_command_line_inner(params, ctx);
        } catch (Exception e) {
            throw (Exception) e;
            //throw new ParserException(String.format("%s:%s", e.getClass().getName(), e.toString()));
        }
        return args;
    }

    public NameSpaceEx parse_command_line(String params[]) throws Exception {
        return this.parse_command_line(params, null);
    }
}