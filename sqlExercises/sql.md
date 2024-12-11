# Homework 1
## Q1
The person relation contains 284 entries with a registered death date after ‘2010-02-
01’. How many entries do not have a registered death date?
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
In the database, there are 46 movies in the French language for which the average
height of all the people involved is greater than 185 centimeters (ignoring people with
unregistered height). What is the number of movies in the Portuguese language for
which the average height of all people involved is greater than 175 centimeters?
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
The movie genre relation does not have a primary key, which can lead to a movie
having more than one entry with the same genre. As a result, there are 14 movies
in movie genre that have the genre ‘Action’ assigned to them more than once. How
many movies in movie genre have the genre ‘Thriller’ assigned to them more than
once?
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

