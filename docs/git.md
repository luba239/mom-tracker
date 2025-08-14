# Открытия, связанные с системой контроля версий

### .gitignore
Оказывается, его можно не выдумывать с нуля, а взять готовый в github:
```shell
curl -o .gitignore https://raw.githubusercontent.com/github/gitignore/main/Android.gitignore
git add .gitignore
```
Правда, там нет `.DS_Store` - мне кажется, имеет смысл добавить