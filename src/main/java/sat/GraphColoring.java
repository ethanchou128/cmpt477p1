package sat;

import com.microsoft.z3.IntExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import java.io.*;
import java.util.*;

public class GraphColoring {

    // public static void main(String[] args) {
    //     String inPath = args[0];
    //     String outPath = args[1];
    //     throw new RuntimeException("To be implemented");
    // }
    public static void main(String[] args) {
        try {
            // Create a Z3 context
            Context ctx = new Context();

            // Declare integer variables
            IntExpr x = ctx.mkIntConst("x");
            IntExpr y = ctx.mkIntConst("y");

            // Build constraints
            BoolExpr constraint1 = ctx.mkGt(ctx.mkAdd(x, y), ctx.mkInt(3)); // x + y > 5
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

            String inPath = args[0];
            String outPath = args[1];
            System.out.println(inPath);
            System.out.println(outPath);

            int numVertices = 0;
            int numColours = 0;
            
            // start the process of reading each of the nodes.
            List<Edge> edges = new ArrayList<>();
            try (Scanner scanner = new Scanner(new File(inPath))) {
                // the first row of the input can be assumed to be the number of nodes and colours.
                numVertices = scanner.nextInt();
                numColours = scanner.nextInt();

                while (scanner.hasNextInt()) {
                    int newU = scanner.nextInt();
                    int newV = scanner.nextInt();
                    Edge newEdge = new Edge(newU, newV);
                    edges.add(newEdge);
                }
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + inPath);
            }
            System.out.println(edges);

            // declare the boolean variable array that will correspond to color(v) = c_x.
            IntExpr[] colours = new IntExpr[numVertices + 1];

            // add constraints for each of the vertices; ensuring the colour is 1 <= v <= M
            for (int i = 1; i <= numVertices; i++) {
                colours[i] = ctx.mkIntConst("colour_" + i);
                BoolExpr colourNumberConstraint = ctx.mkAnd(
                    ctx.mkGe(colours[i], ctx.mkInt(1)),
                    ctx.mkLe(colours[i], ctx.mkInt(numColours))
                );
                solver.add(colourNumberConstraint);
            }

            // add constraints for each edge to ensure the vertices which they are connecting are not the same colour.
            for (Edge edge : edges) {
                BoolExpr edgeColourConstraint = ctx.mkNot(ctx.mkEq(colours[edge.u], colours[edge.v]));
                solver.add(edgeColourConstraint);
            }

            ctx.close();
        } catch (Z3Exception e) {
            e.printStackTrace();
        }
    }

    public static class Edge {
        public final int u;
        public final int v;

        public Edge(int u, int v) {
            this.u = u;
            this.v = v;
        }

        @Override
        public String toString() {
            return u + " - " + v;
        }
    }
}
