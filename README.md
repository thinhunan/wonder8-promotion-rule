[中文说明](README_CN.md)
#### overall
As one of the most complex activities in the sales process, I have not found a good 'marketing promotion' infrastructure. So, the Wonder8.promotion engine comes.

##### Features:

- Support automatically applying the maximum discount from a bunch of marketing rules for a batch of products selected by the user;
- Can apply multiple rules at the same time. The rules can be AND or OR, and the priority of rule combinations can be set;
- Rules can be grouped. The first group is applied first, followed by the second group;
  - It can be limited that the first group of discounts must be applied before calculating the next group of discounts;
  - It can also compare the optimal combination of each group of discount methods;
- Find the best discount with multiple rule matching methods:
    - The best match only matches the rule once;
    - The best single rule matches multiple times;
    - The best multiple rules match multiple times;
- It can support things like buying 12 bottles of water to combine into two boxes of water (another SKU), and two boxes of water can apply another rule;
- It can calculate what products the user should add to get the next discount;
- Provide both server-side Java implementation and client-side JS implementation, so that the client can get the discount results in real time after the discount rules are released;
- Based on specially designed string expressions, various perverted combination gameplay can be flexibly and intuitively expressed, and Builder and Interpreter are provided to convert between strings and structured objects;
- The code structure is clear, and it is more convenient to expand functions and expand various rule combination scenarios.



