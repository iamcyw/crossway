/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package crossway.utils;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * <p>类型转换工具类</p>
 * <p>调用端时将类描述转换为字符串传输。服务端将字符串转换为具体的类</p>
 * <pre>
 *     保证传递的时候值为可阅读格式，而不是jvm格式（[Lxxx;）：
 *     普通：java.lang.String、java.lang.String[]
 *     基本类型：int、int[]
 *     内部类：crossway.example.Inner、crossway.example.Inner[]
 *     匿名类：crossway.example.Xxx$1、crossway.example.Xxx$1[]
 *     本地类：crossway.example.Xxx$1Local、crossway.example.Xxx$1Local[]
 *     成员类：crossway.example.Xxx$Member、crossway.example.Xxx$Member[]
 * 同时Class.forName的时候又会解析出Class。
 * </pre>
 * <p>
 */
public class ClassTypeUtils {

    /**
     * 通用描述转JVM描述
     *
     * @param canonicalName 例如 int[]
     * @return JVM描述 例如 [I;
     */
    public static String canonicalNameToJvmName(String canonicalName) {
        boolean isArray = canonicalName.endsWith("[]");
        if (isArray) {
            String t = ""; // 计数，看上几维数组
            while (isArray) {
                canonicalName = canonicalName.substring(0, canonicalName.length() - 2);
                t += "[";
                isArray = canonicalName.endsWith("[]");
            }
            if ("boolean".equals(canonicalName)) {
                canonicalName = t + "Z";
            } else if ("byte".equals(canonicalName)) {
                canonicalName = t + "B";
            } else if ("char".equals(canonicalName)) {
                canonicalName = t + "C";
            } else if ("double".equals(canonicalName)) {
                canonicalName = t + "D";
            } else if ("float".equals(canonicalName)) {
                canonicalName = t + "F";
            } else if ("int".equals(canonicalName)) {
                canonicalName = t + "I";
            } else if ("long".equals(canonicalName)) {
                canonicalName = t + "J";
            } else if ("short".equals(canonicalName)) {
                canonicalName = t + "S";
            } else {
                canonicalName = t + "L" + canonicalName + ";";
            }
        }
        return canonicalName;
    }

    public static Class<?> getActualTypeArgument(Class<?> clazz, int index) {
        Class<?> entitiClass = null;
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass)
                    .getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                entitiClass = (Class<?>) actualTypeArguments[index];
            }
        }

        return entitiClass;
    }

    /**
     * String转Class
     *
     * @param typeStr 对象描述
     * @return Class[]
     */
    public static Class getClass(String typeStr) {
        // todo cache
        Class clazz;
        if ("void".equals(typeStr)) {
            clazz = void.class;
        } else if ("boolean".equals(typeStr)) {
            clazz = boolean.class;
        } else if ("byte".equals(typeStr)) {
            clazz = byte.class;
        } else if ("char".equals(typeStr)) {
            clazz = char.class;
        } else if ("double".equals(typeStr)) {
            clazz = double.class;
        } else if ("float".equals(typeStr)) {
            clazz = float.class;
        } else if ("int".equals(typeStr)) {
            clazz = int.class;
        } else if ("long".equals(typeStr)) {
            clazz = long.class;
        } else if ("short".equals(typeStr)) {
            clazz = short.class;
        } else {
            String jvmName = canonicalNameToJvmName(typeStr);
            clazz = ClassUtils.forName(jvmName);
        }
        return clazz;
    }

    /**
     * Class[]转String[]
     *
     * @param typeStrs 对象描述[]
     * @return Class[]
     */
    public static Class[] getClasses(String[] typeStrs) throws RuntimeException {
        if (CommonUtils.isEmpty(typeStrs)) {
            return new Class[0];
        } else {
            Class[] classes = new Class[typeStrs.length];
            for (int i = 0; i < typeStrs.length; i++) {
                classes[i] = getClass(typeStrs[i]);
            }
            return classes;
        }
    }

    /**
     * Class转String<br> 注意，得到的String可能不能直接用于Class.forName，请使用getClass(String)反向获取
     *
     * @param clazz Class
     * @return 对象
     * @see #getClass(String)
     */
    public static String getTypeStr(Class clazz) {
        if (clazz.isArray()) {
            // 原始名字：[Ljava.lang.String;
            String name = clazz.getName();
            // java.lang.String[]
            return jvmNameToCanonicalName(name);
        } else {
            return clazz.getName();
        }
    }

    /**
     * Class[]转String[] <br> 注意，得到的String可能不能直接用于Class.forName，请使用getClasses(String[])反向获取
     *
     * @param types Class[]
     * @return 对象描述
     * @see #getClasses(String[])
     */
    public static String[] getTypeStrs(Class[] types) {
        return getTypeStrs(types, false);
    }

    /**
     * Class[]转String[] <br> 注意，得到的String可能不能直接用于Class.forName，请使用getClasses(String[])反向获取
     *
     * @param types     Class[]
     * @param javaStyle JDK自带格式，例如 int[], true的话返回 [I; false的话返回int[]
     * @return 对象描述
     * @see #getClasses(String[])
     */
    public static String[] getTypeStrs(Class[] types, boolean javaStyle) {
        if (CommonUtils.isEmpty(types)) {
            return StringUtils.EMPTY_STRING_ARRAY;
        } else {
            String[] strings = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                strings[i] = javaStyle ? types[i].getName() : getTypeStr(types[i]);
            }
            return strings;
        }
    }

    /*
     * 获取泛型类Class对象，不是泛型类则返回null
     */

    /**
     * JVM描述转通用描述
     *
     * @param jvmName 例如 [I;
     * @return 通用描述 例如 int[]
     */
    public static String jvmNameToCanonicalName(String jvmName) {
        boolean isArray = jvmName.charAt(0) == '[';
        if (isArray) {
            String cnName = StringUtils.EMPTY; // 计数，看上几维数组
            int i = 0;
            for (; i < jvmName.length(); i++) {
                if (jvmName.charAt(i) != '[') {
                    break;
                }
                cnName += "[]";
            }
            String componentType = jvmName.substring(i, jvmName.length());
            if ("Z".equals(componentType)) {
                cnName = "boolean" + cnName;
            } else if ("B".equals(componentType)) {
                cnName = "byte" + cnName;
            } else if ("C".equals(componentType)) {
                cnName = "char" + cnName;
            } else if ("D".equals(componentType)) {
                cnName = "double" + cnName;
            } else if ("F".equals(componentType)) {
                cnName = "float" + cnName;
            } else if ("I".equals(componentType)) {
                cnName = "int" + cnName;
            } else if ("J".equals(componentType)) {
                cnName = "long" + cnName;
            } else if ("S".equals(componentType)) {
                cnName = "short" + cnName;
            } else {
                // 对象的 去掉L
                cnName = componentType.substring(1, componentType.length() - 1) + cnName;
            }
            return cnName;
        }
        return jvmName;
    }
}
