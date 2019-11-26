
public class ClassParameterTest {

	public static void main(String[] args) {
		Class2 class2 = new Class2();
		test1(class2, class2, class2);
	}
	
	public static void test1(Interface1 a, Class1 b, Class2 c) {
		System.out.println(a.a());
		System.out.println(b.a());
		System.out.println(c.a());
	}
	
}

interface Interface1 {
	int a();
}

abstract class Class1 implements Interface1 {
	int b() {return 12345;}
	abstract int c();
}

class Class2 extends Class1 {
	@Override public int a() {
		return c();
	}
	@Override int c() {
		return b();
	}
}