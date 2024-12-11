# Homework 1
## Q1
### Part 1
```sql
SELECT count(*)
FROM person
WHERE deathdate > '2010-02-01';
```
### Part 2
```sql
SELECT count(*)
FROM person
WHERE deathdate IS NULL;
```

## Q2
### Part 1
```sql
SELECT COUNT(*) 
FROM (
    SELECT movieId, AVG(P.height) 
    FROM Involved I
    JOIN person P ON I.personId = P.ID
    JOIN movie M ON I.movieId = M.id
    WHERE language = 'French' 
    GROUP BY I.movieId
    HAVING AVG(P.height) > 185
) X;
```

### Part 2
```sql
SELECT COUNT(*) 
FROM (
    SELECT movieId, AVG(P.height) 
    FROM Involved I
    JOIN person P ON I.personId = P.ID
    JOIN movie M ON I.movieId = M.id
    WHERE language = 'Portuguese' 
    GROUP BY I.movieId
    HAVING AVG(P.height) > 175
) X;
```

## Q3
### Part 1
```sql
SELECT COUNT(*)
FROM (
    SELECT movieId
    FROM movie_genre
    WHERE genre = 'Action'  -- Specify the desired genre
    GROUP BY movieId
    HAVING COUNT(*) > 1
) X;
```

### Part 2
```sql
SELECT COUNT(*)
FROM (
    SELECT movieId
    FROM movie_genre
    WHERE genre = 'Thriller'  -- Specify the desired genre
    GROUP BY movieId
    HAVING COUNT(*) > 1
) X;
```

# Homework 4

## Q1 - In the database, 353 songs have a duration of at least 10 minutes. What is the average duration of songs, in minutes, that have a duration between 5 and 25 minutes, inclusive? Round the number of minutes (ROUND(...)).

### Part 1


### Part 2


## Q2 - What is the total duration in minutes of all explicit songs in the database? Round the number of minutes (ROUND(...)).

### Part 1


## Q3 - The database contains just 5 songs released in 1953. What is the average number of songs released in a year? Round the number of songs (ROUND(...)).

*Note: This is a very simple query. Try also to answer which year had the largest number of songs. Observe how much harder this query is!*

### Part 1


### Part 2


## Q4 - The database contains multiple albums by the artist Queen. Each album has a different average song duration, with the maximum average song duration of an album by Queen being 354 seconds. What is the maximum average song duration (in seconds) of an album by Miles Davis?

*Note: The output of the maximum average song duration is rounded ROUND(...)*

### Part 1


### Part 2


## Q5 - There are 938 song titles that have been used for at least 2 songs, making up a total of 2072 songs with those titles. How many songs have a title that has been used for at least 4 songs?

### Part 1


### Part 2


## Q6 - How many songs have been released after 2010 or belong to an album released in January.

### Part 1


## Q7 - There are 1147 Albums with more than 1 song and none of them are Explicit. How many Albums consists of more than 1 song with all songs being Explicit?

### Part 1


### Part 2


## Q8 - The highest number of genres covered within an Album is 5. In the database, there is only one Album that has this amount of genres. What is the name of this Album?

*Note: Write your query to be capable of finding all albums that have the highest number of genres. (No hardcoded values)*

### Part 1


### Part 2