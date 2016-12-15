package com.github.unchama.seichiassist.data;

//ガチャデータ読み込みテスト
public class GachaDataTest {
	public String inventory;
	public double probability;
	public int amount;
	public GachaDataTest(){
		inventory = null;
		probability = 0.0;
		amount = 0;
	}
	public GachaDataTest(String _inventory,double _probability,int _amount){
		inventory = _inventory;
		probability = _probability;
		amount = _amount;
	}
	public void outputdata(){
		System.out.println("String[data.inventory]");
		System.out.println(inventory);
		System.out.println("double[data.probability]");
		System.out.println(probability);
		System.out.println("int[data.amount]");
		System.out.println(amount);
		System.out.println("");
	}
}
