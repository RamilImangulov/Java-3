public class Main {

    public static void main(String[] args) {
	Box<Apple> appleBox = new Box<>();
	Apple a1 = new Apple();
	Apple a2 = new Apple();
	Apple a3 = new Apple();
	//кидаем яблоки в корзину
    appleBox.addFruit(a1);
    appleBox.addFruit(a2);
    appleBox.addFruit(a3);

    //вес коробки
        System.out.println("\n Вес коробки с яблоками равен " + appleBox.getTotalWeight());

    Box<Orange> orangeBox = new Box<>();
    Orange o1 = new Orange();
    Orange o2 = new Orange();
    Orange o3 = new Orange();

    //кидаем апельсины в коробку
    orangeBox.addFruit(o1);
    orangeBox.addFruit(o2);
    orangeBox.addFruit(o3);

    //вес коробки с апельсинами
        System.out.println("\n Вес коробки с апельсинами равен " + orangeBox.getTotalWeight());

    //сравниваем коробки
        System.out.println("\n Результат сравнения коробок " + appleBox.compare(orangeBox));

     //создаем пустую коробку
     Box<Apple> newAppleBox = new Box<>();
     //перекидываем в новую коробку яблоки
     appleBox.replaceAllFruitsToOtherBox(newAppleBox);

     //созданим еще одно яблоко и закинем в новую коробку и проверим вес
     Apple a4 = new Apple();
     newAppleBox.addFruit(a4);
        System.out.println("\n Вес новой коробки равен " + newAppleBox.getTotalWeight());


    }

}
