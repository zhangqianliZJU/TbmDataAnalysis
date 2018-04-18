package com.zhangqianli.algorythm;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupAttributes {
	private static HashMap<String, ArrayList<String>> groupAttributes;

	static {
		buildHashMap();
		System.out.println("静态初始化结束");
	}
	@SuppressWarnings("unchecked")
	private static void buildHashMap() {
		groupAttributes = new HashMap<>();
		ArrayList<String> temp = new ArrayList<>();
		temp.add("attr178");// 电机电流
		temp.add("attr179");
		temp.add("attr180");
		temp.add("attr181");
		temp.add("attr182");
		temp.add("attr183");
		temp.add("attr184");
		temp.add("attr9");
		temp.add("attr10");
		temp.add("attr11");
		groupAttributes.put("motor_current", (ArrayList<String>) temp.clone());
		temp.clear();
		temp.add("attr12");
		temp.add("attr13");
		temp.add("attr14");
		temp.add("attr15");
		temp.add("attr16");
		temp.add("attr17");
		temp.add("attr18");
		temp.add("attr19");
		temp.add("attr20");
		temp.add("attr21");
		groupAttributes.put("motor_torque", (ArrayList<String>) temp.clone());
		temp.clear();
		temp.add("attr22");
		temp.add("attr23");
		temp.add("attr24");
		temp.add("attr25");
		temp.add("attr26");
		temp.add("attr27");
		temp.add("attr28");
		temp.add("attr29");
		temp.add("attr30");
		temp.add("attr31");
		groupAttributes.put("motor_frequence", (ArrayList<String>) temp.clone());
		temp.clear();
		temp.add("attr32");
		temp.add("attr33");
		temp.add("attr34");
		temp.add("attr35");
		temp.add("attr36");
		temp.add("attr37");
		temp.add("attr38");
		temp.add("attr39");
		temp.add("attr40");
		temp.add("attr41");
		groupAttributes.put("motor_power", (ArrayList<String>) temp.clone());
		temp.clear();
		temp.add("attr42");
		temp.add("attr43");
		temp.add("attr44");
		temp.add("attr45");
		temp.add("attr46");
		temp.add("attr47");
		temp.add("attr48");
		temp.add("attr49");
		temp.add("attr50");
		temp.add("attr51");
		groupAttributes.put("motor_temp", (ArrayList<String>) temp.clone());
		temp.clear();
		System.out.println("我被正确初始化了！");
	}

	public static HashMap<String, ArrayList<String>> getHashMap() {
		//buildHashMap();
		return groupAttributes;
	}

	public static void main(String[] args) {
		System.out.println(GroupAttributes.getHashMap());
	}
}
