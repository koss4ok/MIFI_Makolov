import java.util.Scanner;

public class Main {
    enum  Operations{
        sum,
        subtract,
        multiply,
        divide,
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while (true){

            double operand1 = Double.NaN, operand2 = Double.NaN;
            Operations operation = null;
            do{
                System.out.println("Input first operand");
                var tmp = scanner.next();
                if(tmp.matches("-?\\d+(\\.\\d+)?"))
                    operand1 = Double.parseDouble((tmp));
                else
                    System.out.println("You should use only digits and dot. Try again.");
            }while (Double.isNaN(operand1));

            do{
                System.out.println("Input operation");
                var s = scanner.next().trim();
                switch(s){
                    case "+":{
                       operation = Operations.sum;
                    }break;
                    case "-":{
                        operation = Operations.subtract;
                    }break;
                    case "*":{
                        operation = Operations.multiply;
                    }break;
                    case "/":{
                        operation = Operations.divide;
                    }break;
                    default:{
                        System.out.println("You should input an operation (+, -, /, *). Try again.");
                    }break;
                }
            }while (operation == null);

            do{
                System.out.println("Input second operand");
                var tmp = scanner.next();
                if(tmp.matches("-?\\d+(\\.\\d+)?"))
                    operand2 = Double.parseDouble((tmp));
                else
                    System.out.println("You should use only digits and dot. Try again.");
            }while (Double.isNaN(operand2));


            double result;
            if(operation == Operations.sum){
                result = operand1 + operand2;
            }
            else if(operation == Operations.subtract){
                result = operand1 - operand2;
            }
            else if(operation == Operations.multiply){
                result = operand1 * operand2;
            }
            else {
                result = operand1 / operand2;
            }
            System.out.println("Result = "+ result);

            var answer = "";
            while (answer.isEmpty()){
                System.out.println("Exit? (y/n)");

                answer = scanner.next();
                if(answer.equals("y")){
                    return;
                }
                else if (answer.equals("n")) {
                    break;
                }
            }
        }
    }
}
    