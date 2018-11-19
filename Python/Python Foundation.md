
## Numbers




```python
# Hex
from fractions import Fraction
import math
num = 0xfe
print(num)
```

    254



```python
# Binary
num = 0b10010
print(num)

```

    18



```python
# Binary string representation of a number
bin(34)
```




    '0b100010'




```python
# Byte sequence
b'\xfe'

```




    b'\xfe'




```python
# Some math
2 ** 2048
```




    32317006071311007300714876688669951960444102669715484032130345427524655138867890893197201411522913463688717960921898019494119559150490921095088152386448283120630877367300996091750197750389652106796057638384067568276792218642619756161838094338476170470581645852036305042887575891541065808607552399123930385521914333389668342420684974786564569494856176035326322058077805659331026192708460314150258592864177116725943603718461857357598351152301645904403697613233287231227125684710820209725157101726931323469678542580656697935045997268352998638215525166389437335543602135433229604645318478604952148193555853611059596230656




```python
math.factorial(32)
```




    263130836933693530167218012160000000




```python
# Floor division
# -- full hours
7385 // 3600  # => 2
```




    2




```python
# -- remaining seconds
7385 % 3600  # => 185
```




    185




```python
# Get both quotient and reminder
full_hours, remaining_seconds = divmod(7385, 3600)
full_hours, remaining_seconds  # => 2, 185

```




    (2, 185)




```python
# True division - returns floating-point
7385 / 3600  # => 2.051388888888889
```




    2.051388888888889




```python
# -- same thing, using Fraction
f = Fraction(7385, 3600)
float(f)
```




    2.051388888888889



## Strings

See also:

```

import re
from string import punctuation, whitespace
title = 'Recipe 5: Rewriting, and the Immutable String'

# -- 0-based position
title.index(':')  # => 8

# Slice
title[10:]

# -- upper boundary is exclusive
caption, description = title[:8], title[10:]
caption
description

title.replace(' ', '_')

# Replace all punctuation characters
clean = title
for character in whitespace + punctuation:
    clean = clean.replace(character, '_')
clean

# Remove leading and trailing
'_hello_world__'.strip('_')  # => 'hello_world'
'-#-hello-#-'.strip('#-')   # => 'hello'

# Upper/lower case
'hello'.upper()
'SomEtHinG'.lower()


# Slice at the end
title[-4:]  # => 'ring'

#
'123'.isnumeric()
'abc'.isnumeric()


# Match with regex
# r prefix instructs Python not to look at the \ and not to replace them.
parse_ingredient = re.compile(r'(\w+):\s+(\d+)\s+(\w+)')
matches = parse_ingredient.match("eggs: 2 pieces")
# -- Is pattern matched?
matches is None  # => False
# -- Get matched groups
matches.groups()

# -- Access matched groups
matches[0]  # => full match: 'eggs: 2 pieces'
matches[1]  # => first group: 'eggs'

# Naming groups
parse_ingredient = re.compile(
    r'(?P<ingredient>\w+):\s+(?P<amount>\d+)\s+(?P<unit>\w+)')
matches = parse_ingredient.match("eggs: 2 pieces")

# -- Access by group name
matches.group('ingredient')  # => 'eggs'
matches['ingredient']  # => 'eggs'
# -- Index based group access still works
matches.group(1)  # => 'eggs'
matches[1]  # => 'eggs'
```


### Template Strings

See also:
* https://realpython.com/python-string-formatting/



```python
# Template strings
# -- type specifier is optional
template = '{id:s} : {location:s} : {max_temp:d} / {min_temp:d} / {precipitation:f}'
template.format(id='AMS', location="Amsterdam", max_temp=19,
                min_temp=10, precipitation=0.30)
```




    'AMS : Amsterdam : 19 / 10 / 0.300000'




```python
# -- Specify formatting options
template = '{id:3s} : {location:15s} : {max_temp:2d} / {min_temp:2d} / {precipitation:5.2f}'
template.format(id='AMS', location="Amsterdam", max_temp=19,
                min_temp=10, precipitation=0.30)

```




    'AMS : Amsterdam       : 19 / 10 /  0.30'




```python
# -- use a dictionary with format_map
data = dict(id='AMS', location="Amsterdam", max_temp=19,
            min_temp=10, precipitation=0.30)
template.format_map(data)

```




    'AMS : Amsterdam       : 19 / 10 /  0.30'




```python
# Automatically build a dictionary of all local variables
# vars() could be called in any scope. Here I put it in function
# to get smaller output.
def some_func(a, b, c):
    return vars()

some_func('Hello', 'world','!')

```




    {'c': '!', 'b': 'world', 'a': 'Hello'}




```python
# Use Unicode name
'\N{black spade suit}'

```




    '♠'




```python
# String template with positional arguments
'{} - {}'.format('key', 'value')
```




    'key - value'




```python
# With index specified
'{1} - {0}'.format('key', 'value')
```




    'value - key'


