/* https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java
 * Copyright 2015 Olivier Grégoire.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Spliterators;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * A tool to randomly select elements from collections.
 *
 * <p>
 * Example usages:
 *
 * <pre><code>
 * Random random = ...
 * Map&lt;String, Double&gt; stringWeights = new Hash&lt;&gt;();
 * stringWeights.put("a", 4d);
 * stringWeights.put("b", 3d);
 * stringWeights.put("c", 12d);
 * stringWeights.put("d", 1d);
 *
 * RandomSelector&lt;String&gt; selector = RandomSelector.weighted(stringWeights.keySet(), s -&gt; stringWeights.get(s));
 * List&lt;String&gt; selection = new ArrayList&lt;&gt;();
 * for (int i = 0; i &lt; 10; i++) {
 *   selection.add(selector.next(random));
 * }
 * </code></pre>
 *
 *
 * @author Olivier Grégoire
 * @param <T>
 */
public final class RandomSelector<T> {

  /**
   * Creates a new random selector based on a uniform distribution.
   *
   * <p>
   * A copy of <tt>elements</tt> is kept, so any modification to <tt>elements</tt> will not be
   * reflected in returned values.
   *
   * @param <T>
   * @param elements
   * @return
   * @throws IllegalArgumentException if <tt>elements</tt> is empty.
   */
  public static <T> RandomSelector<T> uniform(final Collection<T> elements)
      throws IllegalArgumentException {
    requireNonNull(elements, "collection must not be null");

    final int size = elements.size();
    final T[] els = elements.toArray((T[]) new Object[size]);

    return new RandomSelector<>(els, r -> r.nextInt(size));
  }

  /**
   * Creates a random selector among <tt>elements</tt> where the elements have a weight defined by
   * <tt>weighter</tt>.
   *
   * <p>
   * A copy of <tt>elements</tt> is kept, so any modification to <tt>elements</tt> will not be
   * reflected in returned values.
   *
   * @param <T>
   * @param elements
   * @param weighter
   * @return
   * @throws IllegalArgumentException if <tt>elements</tt> is empty or if <tt>weighter</tt> returns
   * a negative value or <tt>0</tt>.
   */
  public static <T> RandomSelector<T> weighted(
      final Collection<T> elements,
      final ArrayList<Double> weighter)
      throws IllegalArgumentException {
    requireNonNull(elements, "elements must not be null");
    requireNonNull(weighter, "weighter must not be null");

    final int size = elements.size();
    final T[] elementArray = elements.toArray((T[]) new Object[size]);

    double totalWeight = 0d;
    final double[] discreteProbabilities = new double[size];
    for (int i = 0; i < size; i++) {
      final double weight = weighter.get(i);
      discreteProbabilities[i] = weight;
      totalWeight += weight;
    }
    for (int i = 0; i < size; i++) {
      discreteProbabilities[i] /= totalWeight;
    }
    return new RandomSelector<>(elementArray, new RandomWeightedSelection(discreteProbabilities));
  }

  private final T[] elements;
  private final ToIntFunction<Random> selection;
  public int currentIndex;

  RandomSelector(final T[] elements, final ToIntFunction<Random> selection) {
    this.elements = elements;
    this.selection = selection;
    this.currentIndex = 0;
  }

  /**
   * Returns the next element using <tt>random</tt>.
   *
   * @param random
   * @return
   */

  // ORIGINAL! 
  // public T next(final Random random) {
  //   System.out.println(selection.applyAsInt(random));
  //   return elements[selection.applyAsInt(random)];
  // }
   
  public T next(final Random random) {
    int i = selection.applyAsInt(random);
    this.currentIndex = i;
    return elements[i];
  }

  /**
   * Returns a stream of elements using <tt>random</tt>. The stream must use a terminal operation to
   * become closed and free the resources it's been using.
   *
   * <p>
   * Even though this instance is thread-safe and for performance reasons, it is recommended to use
   * a different stream per thread given that Random has performance drawbacks in multi-threaded
   * environments.
   *
   * @param random
   * @return
   */
  public Stream<T> stream(final Random random) {
    requireNonNull(random, "random must not be null");
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(
            new BaseIterator(random),
            IMMUTABLE | ORDERED
        ),
        false
    );
  }

  private class BaseIterator implements Iterator<T> {

    private final Random random;

    BaseIterator(final Random random) {
      this.random = random;
    }

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public T next() {
      return RandomSelector.this.next(this.random);
    }
  }

  private static class RandomWeightedSelection implements ToIntFunction<Random> {
    // Alias method implementation O(1)
    // using Vose's algorithm to initialize O(n)

    private final double[] probabilities;
    private final int[] alias;

    RandomWeightedSelection(final double[] probabilities) {
      final int size = probabilities.length;

      final double average = 1d / size;
      final int[] small = new int[size];
      int smallSize = 0;
      final int[] large = new int[size];
      int largeSize = 0;

      for (int i = 0; i < size; i++) {
        if (probabilities[i] < average) {
          small[smallSize++] = i;
        } else {
          large[largeSize++] = i;
        }
      }

      final double[] pr = new double[size];
      final int[] al = new int[size];
      this.probabilities = pr;
      this.alias = al;

      while (largeSize != 0 && smallSize != 0) {
        final int less = small[--smallSize];
        final int more = large[--largeSize];
        pr[less] = probabilities[less] * size;
        al[less] = more;
        probabilities[more] += probabilities[less] - average;
        if (probabilities[more] < average) {
          small[smallSize++] = more;
        } else {
          large[largeSize++] = more;
        }
      }
      while (smallSize != 0) {
        pr[small[--smallSize]] = 1d;
      }
      while (largeSize != 0) {
        pr[large[--largeSize]] = 1d;
      }
    }

    @Override
    public int applyAsInt(final Random random) {
      final int column = random.nextInt(probabilities.length);
      return random.nextDouble() < probabilities[column]
          ? column
          : alias[column];
    }
  }
}