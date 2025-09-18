package sat;

import com.microsoft.z3.*;

public class TestZ3Class {
    public static void main(String[] args) {
        try {
            // Create a Z3 context
            Context ctx = new Context();

            // Declare integer variables
            IntExpr x = ctx.mkIntConst("x");
            IntExpr y = ctx.mkIntConst("y");

            // Build constraints
            BoolExpr constraint1 = ctx.mkGt(ctx.mkAdd(x, y), ctx.mkInt(5)); // x + y > 5
            BoolExpr constraint2 = ctx.mkLt(x, ctx.mkInt(3));               // x < 3

            // Create solver and add constraints
            Solver solver = ctx.mkSolver();
            solver.add(constraint1);
            solver.add(constraint2);

            // Check satisfiability
            if (solver.check() == Status.SATISFIABLE) {
                System.out.println("SAT");
                Model model = solver.getModel();
                System.out.println("x = " + model.evaluate(x, false));
                System.out.println("y = " + model.evaluate(y, false));
            } else {
                System.out.println("UNSAT");
            }

            ctx.close();
        } catch (Z3Exception e) {
            e.printStackTrace();
        }
    }
}